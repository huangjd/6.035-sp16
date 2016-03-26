package edu.mit.compilers.codegen;

import java.util.*;
import java.util.AbstractMap.SimpleEntry;

public class IRBuilder {

  BasicBlock currentBB;
  ArrayList<BasicBlock> basicBlocks;

  public IRBuilder() {
  }

  public void insertFunction() {
    basicBlocks = new ArrayList<BasicBlock>();
  }

  static private int bbcounter = 0;
  public BasicBlock createBasicBlock() {
    BasicBlock bb = new BasicBlock(".bb." + Integer.toString(bbcounter++));
    return bb;
  }

  public BasicBlock createBasicBlock(String label) {
    return new BasicBlock(label);
  }

  public void insertBasicBlock(BasicBlock bb) {
    basicBlocks.add(bb);
  }

  public void setCurrentBasicBlock(BasicBlock bb) {
    currentBB = bb;
  }

  public BasicBlock getCurrentBasicBlock() {
    return currentBB;
  }

  static private int registerID = Register.VIRT_REG_START;
  public Value allocateRegister() {
    return new Value(new Register(registerID++));
  }

  public Value allocateRegister(int placementHint) {
    return new Value(new Register(registerID++, placementHint));
  }

  public Instruction emitInstruction(Instruction inst) {
    currentBB.add(inst);
    return inst;
  }

  public Value emitMov(Value src, Value dest) {
    currentBB.add(new Instruction(Opcode.MOV, src, dest));
    return dest;
  }

  public Value emitMov(long imm, Value dest) {
    currentBB.add(new Instruction(Opcode.MOV, new Immediate(imm).box(), dest));
    return dest;
  }

  public Value emitOp(Opcode op, Value a, Value b) {
    Value val = emitMov(a, allocateRegister());
    currentBB.add(new Instruction(op, b, val));
    return val;
  }

  public Value emitMul(Value a, Value b) {
    Value res = allocateRegister();
    Value saveA = emitMov(Register.RAX.box(), allocateRegister());
    emitMov(a, Register.RAX.box());
    currentBB.add(new Instruction(Opcode.IMUL, b, Register.RAX.box()));
    emitMov(Register.RAX.box(), res);
    emitMov(saveA, Register.RAX.box());
    return res;
  }

  public AbstractMap.SimpleEntry<Value, Value> emitDiv(Value a, Value b) {
    // forced preserve rdx and rax
    Value saveA = allocateRegister();
    Value saveD = allocateRegister();
    Value quot = allocateRegister();
    Value rem = allocateRegister();

    currentBB.add(new Instruction(Opcode.MOV, Register.RAX.box(), saveA));
    currentBB.add(new Instruction(Opcode.MOV, Register.RDX.box(), saveD));
    currentBB.add(new Instruction(Opcode.XOR, Register.RDX.box(), Register.RDX.box()));
    currentBB.add(new Instruction(Opcode.MOV, a, Register.RAX.box()));
    currentBB.add(new Instruction(Opcode.IDIV, b));
    currentBB.add(new Instruction(Opcode.MOV, Register.RAX.box(), quot));
    currentBB.add(new Instruction(Opcode.MOV, Register.RDX.box(), rem));
    currentBB.add(new Instruction(Opcode.MOV, saveD, Register.RDX.box()));
    currentBB.add(new Instruction(Opcode.MOV, saveA, Register.RAX.box()));

    return new SimpleEntry<>(quot, rem);
  }

  public Value emitLoad(Value var) {
    Value reg = allocateRegister();
    currentBB.add(new Instruction(Opcode.MOV, var, reg));
    return reg;
  }

  public Value emitLoad(Value base, Value index) {
    if (!(base.value instanceof Register)) {
      Value temp = allocateRegister(Register.MUST_REG);
      currentBB.add(new Instruction(Opcode.MOV, base, temp));
      base = temp;
    }
    Value res = allocateRegister();
    if (index.value instanceof Immediate || index.value instanceof Symbol) {
      currentBB.add(new Instruction(Opcode.MOV, new Memory(base, null, index, 8).box(), res));
    } else if (index.value instanceof Register) {
      currentBB.add(new Instruction(Opcode.MOV, new Memory(base, index, null, 8).box(), res));
    } else {
      Value temp = allocateRegister(Register.MUST_REG);
      currentBB.add(new Instruction(Opcode.MOV, index, temp));
      currentBB.add(new Instruction(Opcode.MOV, new Memory(base, temp, null, 8).box(), res));
    }
    return res;
  }

  public void emitStore(Value value, Value var) {
    currentBB.add(new Instruction(Opcode.MOV, value, var));
  }

  public void emitStore(Value value, Value base, Value index) {
    if (!(base.value instanceof Register)) {
      Value temp = allocateRegister(Register.MUST_REG);
      currentBB.add(new Instruction(Opcode.MOV, base, temp));
      base = temp;
    }
    if (index.value instanceof Immediate || index.value instanceof Symbol) {
      currentBB.add(new Instruction(Opcode.MOV, value, new Memory(base, null, index, 8).box()));
    } else if (index.value instanceof Register) {
      currentBB.add(new Instruction(Opcode.MOV, value, new Memory(base, index, null, 8).box()));
    } else {
      Value temp = allocateRegister(Register.MUST_REG);
      currentBB.add(new Instruction(Opcode.MOV, index, temp));
      currentBB.add(new Instruction(Opcode.MOV, value, new Memory(base, temp, null, 8).box()));
    }
  }

  public void emitBranch(Value cond, BasicBlock trueBlock, BasicBlock falseBlock) {
    Instruction i1 = new Instruction(Opcode.TEST, cond, cond);
    currentBB.add(i1);
    currentBB.add(new Instruction(Opcode.JNE, new Symbol(trueBlock.label).box()).addDependency(i1, Instruction.NO_RFLAGS_MODIFICATION));
    currentBB.add(new Instruction(Opcode.JMP, new Symbol(falseBlock.label).box()));
  }

  public void emitBranch(BasicBlock br) {
    currentBB.add(new Instruction(Opcode.JMP, new Symbol(br.label).box()));
  }

  public Value emitCmpToBool(Opcode type, Value a, Value b) {
    Value res = allocateRegister();
    currentBB.add(new Instruction(Opcode.MOV, new Immediate(0).box(), res));
    Instruction i1 = new Instruction(Opcode.CMP, a, b);
    currentBB.add(i1);
    currentBB.add(new Instruction(type, res).addDependency(i1, Instruction.NO_RFLAGS_MODIFICATION));
    return res;
  }

  public void emitPrologue(CallingConvention c) {
    int[] regs = c.getCalleeSavedRegs();
    for (int i = 0; i < regs.length; i++) {
      emitMov(new Register(regs[i]).box(), new Memory(Register.RBP.box(), null, -(8 * i + 8), 8).box());
    }
  }

  public void emitEpilogue(CallingConvention c) {
    int[] regs = c.getCalleeSavedRegs();
    for (int i = 0; i < regs.length; i++) {
      emitMov(new Memory(Register.RBP.box(), null, -(8 * i + 8), 8).box(), new Register(regs[i]).box());
    }
  }
}

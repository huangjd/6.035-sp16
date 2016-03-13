package edu.mit.compilers.codegen;

import java.util.*;

import edu.mit.compilers.nodes.Function;

public class IRBuilder {

  BasicBlock currentBB;
  Function currentFunction;

  ArrayList<ArrayList<BasicBlock>> functions;

  public IRBuilder() {
    currentFunction = null;
    functions = new ArrayList<>();
  }

  public BasicBlock beginFunction(Function f) {
    if (currentFunction != null) {
      throw new RuntimeException("Called IRBuilder::beginFunction while compiling a function");
    }
    BasicBlock newbb = new BasicBlock();
    ArrayList<BasicBlock> temp = new ArrayList<>();
    temp.add(newbb);
    functions.add(temp);
    currentFunction = f;
    currentBB = newbb;
    return newbb;
  }

  public void endFunction() {
    if (currentFunction == null) {
      throw new RuntimeException("Called IRBuilder::endFunction while not compiling a function");
    }
    currentFunction = null;
  }

  public BasicBlock createBasicBlock() {
    BasicBlock bb = new BasicBlock();
    functions.get(functions.size() - 1).add(bb);
    return bb;
  }

  public void setCurrentBasicBlock(BasicBlock bb) {
    currentBB = bb;
  }

  public BasicBlock getCurrentBasicBlock() {
    return currentBB;
  }

  public Register emitOp(Opcode op, Register a, Register b) {
    assert(a.type == b.type);
    if (a instanceof Immediate || a instanceof Memory) {
      if (b instanceof Immediate || b instanceof Memory) {
        Register temp = new Register(b.type);
        currentBB.add(new Instruction(Opcode.MOV, null, temp, b));
      }
    } else {
      if (b instanceof Immediate || b instanceof Memory) {

      } else {

      }
    }

    Register res = new Register(a.type);
    currentBB.add(new Instruction(op, Instruction.RegReg, res, new Register[]{a, b}, 0));
    return res;
  }

  public Register emitOp(Opcode op, Register a, Immediate b) {
    assert (a.type == b.type);
    Register res = new Register(a.type);
    currentBB.add(new Instruction(op, Instruction.RegImm, res, new Register[]{a, b}, 0));
    return res;
  }

  public Register emitOp(Opcode op, Register a, Register base, Register index, long offset) {

  }

  public AbstractMap.SimpleEntry<Register, Register> emitDiv(Register a, Register b) {

  }

  public Register emitLoad(Register base, Register index, long offset) {

  }

  public void emitStore(Register value, Register base, Register index, long offset) {

  }

























}

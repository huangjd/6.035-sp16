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
    return bb;
  }

  public void insertBasicBlock(BasicBlock bb) {
    functions.get(functions.size() - 1).add(bb);
  }

  public void setCurrentBasicBlock(BasicBlock bb) {
    currentBB = bb;
  }

  public BasicBlock getCurrentBasicBlock() {
    return currentBB;
  }

  public Register emitOp(Opcode op, Register a, Register b) {
    assert(a.type == b.type);
    Register temp = new Register(b.type);
    if (a instanceof Immediate || a instanceof Memory) {
      if (b instanceof Immediate || b instanceof Memory) {
        currentBB.add(new Instruction(Opcode.MOV, temp, b));
        currentBB.add(new Instruction(op, temp, temp, a));
      } else {
        currentBB.add(new Instruction(op, temp, b, a));
      }
    } else {
      currentBB.add(new Instruction(op, temp, a, b));
    }
    return temp;
  }

  public AbstractMap.SimpleEntry<Register, Register> emitDiv(Register a, Register b) {

  }

  public Register emitLoad(Register base, Register index, long offset) {

  }

  public void emitStore(Register value, Register base, Register index, long offset) {

  }

  public void emitBranch(Register cond, BasicBlock trueBlock, BasicBlock falseBlock) {

  }

  public void emitBranch(BasicBlock br) {

  }

  public Register createPhiNode(AbstractMap.SimpleEntry<Register, BasicBlock> comeFrom) {

  }





















}

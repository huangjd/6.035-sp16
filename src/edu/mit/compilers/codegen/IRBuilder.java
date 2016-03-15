package edu.mit.compilers.codegen;

import java.util.*;

import edu.mit.compilers.common.Var;
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

  public Value emitOp(Opcode op, Value a, Value b) {
    assert(a.type == b.type);
    Value temp = new Value(b.type);
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

  public Value emitMul(Value a, Value b) {

  }

  public AbstractMap.SimpleEntry<Value, Value> emitDiv(Value a, Value b) {

  }

  public Value emitLoad(Var var) {

  }

  public Value emitLoad(Var base, Value index) {

  }

  public void emitStore(Value value, Var var) {

  }

  public void emitStore(Value value, Var base, Value index) {

  }

  public void emitBranch(Value cond, BasicBlock trueBlock, BasicBlock falseBlock) {

  }

  public void emitBranch(Opcode type, BasicBlock trueBlock) {
  }

  public void emitBranch(BasicBlock br) {

  }

  public void emitCmp(Value a, Value b) {

  }

  public Value emitCmpToBool(Opcode type, Value a, Value b) {

  }

  public Value createPhiNode(AbstractMap.SimpleEntry<Value, BasicBlock>[] comeFroms) {

  }

  public void prepareArgument(int nth, Value value) {
  }

  public Value getReturnValue() {
  }

  public void emitInstruction(Instruction inst) {

  }














}

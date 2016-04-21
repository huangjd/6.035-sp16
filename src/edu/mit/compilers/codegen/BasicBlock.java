package edu.mit.compilers.codegen;

import java.util.ArrayList;

public class BasicBlock extends ArrayList<Instruction> {

  String label;
  BasicBlock taken, notTaken;
  ArrayList<BasicBlock> comefroms = new ArrayList<>();
  int priority = priorityCounter++;

  public static int priorityCounter = 0;
  static int id = 0;
  public BasicBlock() {
    super();
    label = ".LBB" + Integer.toString(id);
    id++;
  }

  public BasicBlock(String s) {
    super();
    label = s;
  }

  public void deferPriority() {
    priority = priorityCounter++;
  }

  public BasicBlock add(Operand dest, Op op, Operand a, Operand b) {
    super.add(new Instruction(dest, op, a, b));
    return this;
  }

  public BasicBlock add(Operand dest, Op op, Operand a) {
    super.add(new Instruction(dest, op, a));
    return this;
  }

  public BasicBlock add(Operand dest, Op op) {
    super.add(new Instruction(dest, op));
    return this;
  }

  public BasicBlock add(Op op, Operand a, Operand b) {
    super.add(new Instruction(Value.dummy, op, a, b));
    return this;
  }

  public void addJmp(Op op, BasicBlock b1, BasicBlock b2) {
    assert (op == Op.JG || op == Op.JGE || op == Op.JL || op == Op.JLE || op == Op.JE || op == Op.JNE);
    super.add(new Instruction(Value.dummy, op, new JumpTarget(b1), new JumpTarget(b2)));
    setTaken(b1);
    setNotTaken(b2);
  }

  public void addJmp(BasicBlock b1) {
    super.add(new Instruction(Value.dummy, Op.JMP, new JumpTarget(b1)));
    setTaken(b1);
  }

  public BasicBlock add(Op op, Operand a) {
    super.add(new Instruction(Value.dummy, op, a));
    return this;
  }

  public BasicBlock add(Op op) {
    super.add(new Instruction(Value.dummy, op));
    return this;
  }

  public BasicBlock addISA(Op op, Operand a, Operand b) {
    super.add(new Instruction(op, a, b));
    return this;
  }

  private void setTaken(BasicBlock target) {
    clearTaken();
    taken = target;
    assert (target != null);
    assert (!target.comefroms.contains(this));
    target.comefroms.add(this);
  }

  private void setNotTaken(BasicBlock target) {
    clearNotTaken();
    notTaken = target;
    assert (target != null);
    assert (!target.comefroms.contains(this));
    target.comefroms.add(this);
  }

  public void clearTaken() {
    if (taken != null) {
      boolean result = taken.comefroms.remove(this);
      assert (result);
      taken = null;
    }
  }

  public void clearNotTaken() {
    if (notTaken != null) {
      boolean result = notTaken.comefroms.remove(this);
      assert (result);
      notTaken = null;
    }
  }

  @Override
  public String toString() {
    BasicBlockPrinter p = new BasicBlockPrinter();
    p.traverse(this);
    return p.toString();
  }
}

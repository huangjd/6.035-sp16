package edu.mit.compilers.codegen;

import java.util.ArrayList;

public class BasicBlock extends ArrayList<Instruction> {

  String label;
  BasicBlock taken, notTaken;
  ArrayList<BasicBlock> comefroms = new ArrayList<>();
  int priority = 0;

  static int id = 0;

  int lastVisitedBasicBlockVisitorID = 0;

  public BasicBlock() {
    super();
    label = ".LFB" + Integer.toString(id);
    id++;
  }

  public BasicBlock(String s) {
    super();
    label = s;
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
    super.add(new Instruction(op, a, b));
    return this;
  }

  public BasicBlock add(Op op, Operand a) {
    super.add(new Instruction(op, a));
    return this;
  }

  public BasicBlock add(Op op) {
    super.add(new Instruction(op));
    return this;
  }

  public void setTaken(BasicBlock target) {
    clearTaken();
    taken = target;
    assert (target != null);
    assert (!target.comefroms.contains(this));
    target.comefroms.add(this);
  }

  public void setNotTaken(BasicBlock target) {
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
}

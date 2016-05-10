package edu.mit.compilers.codegen;

import java.util.*;

import edu.mit.compilers.common.Util;

public class LCSE extends BasicBlockTraverser {

  class ExprDesc {
    Op op;
    Operand a;
    Operand b;

    ExprDesc(Op op, Operand a, Operand b) {
      this.op = op;
      this.a = a;
      this.b = b;
    }

    @Override
    public boolean equals(Object arg0) {
      if (!(arg0 instanceof ExprDesc)) {
        return false;
      }
      ExprDesc other = (ExprDesc) arg0;
      if (op != other.op) {
        return false;
      }
      if (a == null && other.a != null || !a.equals(other.a)) {
        return false;
      }
      if (b == null && other.b != null || !b.equals(other.b)) {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode() {
      return 0;
    }

    @Override
    public String toString() {
      return op.toString() + '\t' + (a != null ? a.toString() : "") + (b != null ? ",\t" + b.toString() : "");
    }
  }

  HashMap<ExprDesc, Operand> exprs;
  HashMap<Operand, Operand> rename;

  boolean isArith(Op op) {
    switch (op) {
    case ADD:
    case SUB:
    case IMUL:
    case XOR:
    case CMP:
    case TEST:
    case RANGE:
      return true;
    default:
      return false;
    }
  }

  Operand getOrOrigin(Operand op) {
    Operand r = rename.get(op);
    if (r == null) {
      return op;
    }
    return r;
  }

  @Override
  protected void visit(BasicBlock b) {
    exprs = new HashMap<>();
    rename = new HashMap<>();

    for (int i = 0; i < b.size(); i++) {
      Instruction ins = b.get(i);
      for (Operand op : ins.getDestOperand()) {
        for (Iterator<Map.Entry<ExprDesc, Operand>> it = exprs.entrySet().iterator(); it.hasNext();) {
          Map.Entry<ExprDesc, Operand> ent = it.next();
          ExprDesc e = ent.getKey();
          Operand o = ent.getValue();
          if (op.equals(e.a) || op.equals(e.b) || op.equals(o)) {
            it.remove();
            rename.remove(op);
          }
        }
      }

      if (ins.op.invalidateFlag()) {
        for (Iterator<Map.Entry<ExprDesc, Operand>> it = exprs.entrySet().iterator(); it.hasNext();) {
          Map.Entry<ExprDesc, Operand> ent = it.next();
          ExprDesc e = ent.getKey();
          if (e.op == Op.CMP || e.op == Op.TEST || e.op == Op.RANGE) {
            it.remove();
          }
        }
      }
      if (ins.op == Op.CALL) {
        exprs.clear();
      }

      if (isArith(ins.op)) {
        ExprDesc desc = new ExprDesc(ins.op, getOrOrigin(ins.a), getOrOrigin(ins.b));
        Operand value = exprs.get(desc);
        if (value != null) {

          if (value == Value.dummy) {
            b.set(i, new Instruction(Op.DELETED));
          } else {
            b.set(i, new Instruction(ins.dest, Op.MOV, value));
            rename.put(ins.dest, value);
          }
        } else {

          exprs.put(desc, ins.dest);
        }
      }

      Operand[] readOperand = ins.getReadOperand();
      for (Operand op : ins.getDestOperand()) {
        if (Util.in(readOperand, op)) {

          for (Iterator<Map.Entry<ExprDesc, Operand>> it = exprs.entrySet().iterator(); it.hasNext();) {
            Map.Entry<ExprDesc, Operand> ent = it.next();
            ExprDesc e = ent.getKey();
            Operand o = ent.getValue();
            if (op.equals(e.a) || op.equals(e.b) || op.equals(o)) {
              it.remove();
              rename.remove(op);
            }
          }

        }
      }

    }
  }

}

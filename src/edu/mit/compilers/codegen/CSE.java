package edu.mit.compilers.codegen;

import java.util.*;

public class CSE extends BasicBlockAnalyzeTransformPass {

  HashSet<Operand> globals;

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

  class DivRes extends Operand {
    Operand q;
    Operand r;

    DivRes(Operand q, Operand r) {
      assert (!(q instanceof DivRes) && !(r instanceof DivRes));
      assert (q != null && q != Value.dummy && r != null && r != Value.dummy);
      this.q = q;
      this.r = r;
    }

    @Override
    public Type getType() {
      return null;
    }

    @Override
    public boolean isPointer() {
      return false;
    }

    @Override
    public String toString() {
      return q.toString() + ", " + r.toString();
    }

    @Override
    public boolean equals(Object arg0) {
      return arg0 instanceof DivRes && q.equals(((DivRes) arg0).q) && r.equals(((DivRes) arg0).r);
    }
  }

  class State extends BasicBlockAnalyzeTransformPass.State {

    HashMap<ExprDesc, Operand> exprs;
    HashMap<Operand, Operand> rename;

    State(HashMap<ExprDesc, Operand> exprs, HashMap<Operand, Operand> localrename) {
      this.exprs = exprs;
      this.rename = localrename;
    }

    void replaceOut(Operand old, Operand n) {
      for (ExprDesc e : exprs.keySet()) {
        if (exprs.get(e).equals(old)) {
          exprs.put(e, n);
        }
      }
      for (Operand o : rename.keySet()) {
        if (rename.get(o).equals(old)) {
          rename.put(o, n);
        }
      }
    }

    @Override
    public State merge(BasicBlockAnalyzeTransformPass.State t) {
      State other = (State) t;

      for (Iterator<Entry<ExprDesc, Operand>> it = exprs.entrySet().iterator(); it.hasNext();) {
        Entry<ExprDesc, Operand> e = it.next();
        ExprDesc e1 = e.getKey();
        Operand o1 = e.getValue();
        if (other.exprs.containsKey(e1)) {
          Operand o2 = other.exprs.get(e1);
          if (!o1.equals(o2)) {
            if (o1 instanceof DivRes) {
              assert (o2 instanceof DivRes);
              replaceOut(((DivRes) o1).q, ((DivRes) o2).q);
              replaceOut(((DivRes) o1).r, ((DivRes) o2).r);
            } else if (o1 != Value.dummy) {
              replaceOut(o1, o2);
            }
          }
        } else {
          it.remove();
        }
      }
      for (Iterator<Entry<ExprDesc, Operand>> it = other.exprs.entrySet().iterator(); it.hasNext();) {
        if (!exprs.containsKey(it.next().getKey())) {
          it.remove();
        }
      }

      for (Iterator<Entry<Operand, Operand>> it = rename.entrySet().iterator(); it.hasNext();) {
        Entry<Operand, Operand> e = it.next();
        Operand k1 = e.getKey();
        Operand v1 = e.getValue();
        if (!v1.equals(other.rename.get(k1))) {
          it.remove();
        }
      }

      for (Iterator<Entry<Operand, Operand>> it = other.rename.entrySet().iterator(); it.hasNext();) {
        Entry<Operand, Operand> e = it.next();
        Operand k2 = e.getKey();
        Operand v2 = e.getValue();
        if (!v2.equals(rename.get(k2))) {
          it.remove();
        }
      }
      assert (this.equals(other));
      return this;
    }

    @Override
    protected State clone() {
      return new State((HashMap<ExprDesc, Operand>) exprs.clone(), (HashMap<Operand, Operand>) rename.clone());
    }

    @Override
    public boolean equals(Object arg0) {
      if (!(arg0 instanceof State)) {
        return false;
      }
      State other = (State) arg0;

      if (exprs.size() != other.exprs.size() || rename.size() != other.rename.size()) {
        return false;
      }

      for (ExprDesc expr : exprs.keySet()) {
        if (!exprs.get(expr).equals(other.exprs.get(expr))) {
          return false;
        }
      }
      for (Operand op : rename.keySet()) {
        if (!rename.get(op).equals(other.rename.get(op))) {
          return false;
        }
      }
      return true;
    }

    Operand lookup(Operand op) {
      Operand o = rename.get(op);
      if (o != null) {
        return o;
      }
      return op;
    }

    void invalidate(Operand op) {
      rename.remove(op);
      for (Iterator<Entry<ExprDesc, Operand>> it = exprs.entrySet().iterator(); it.hasNext();) {
        ExprDesc desc = it.next().getKey();
        if (op.equals(desc.a) || op.equals(desc.b)) {
          it.remove();
        }
      }
    }
  }



  @Override
  public State getInitValue() {
    return new State(new HashMap<ExprDesc, Operand>(), new HashMap<Operand, Operand>());
  }

  @Override
  public State analyze(BasicBlock b, BasicBlockAnalyzeTransformPass.State in) {
    State state = ((State) in).clone();
    for (int i = 0; i < b.size(); i++) {
      Instruction ins = b.get(i);
      if (ins.op.isAnnotation()) {
        continue;
      }

      if (ins.dest instanceof BSSObject) {
        globals.add(ins.dest);
      }

      if (ins.a instanceof BSSObject) {
        globals.add(ins.a);
      }

      if (ins.b instanceof BSSObject) {
        globals.add(ins.b);
      }

      if (ins.op == Op.FAKE_CALL) {
        for (Operand op : globals) {
          state.invalidate(op);
        }
      }

      if (ins.op.invalidateFlag()) {
        for (Iterator<Entry<ExprDesc, Operand>> it = state.exprs.entrySet().iterator(); it.hasNext();) {
          Op op = it.next().getKey().op;
          if (op == Op.CMP || op == Op.TEST) {
            it.remove();
          }
        }
      }


      for (Operand dest : ins.getDestOperand()) {
        state.invalidate(dest);
      }


      if (!ins.twoOperand) {
        switch (ins.op) {
        case ADD:
        case SUB:
        case IMUL:
        case XOR:
        case SAL:
        case SAR:
        case NOT:
        case NEG:
        case INC:
        case DEC:
        case LOAD:
          ExprDesc e = new ExprDesc(ins.op, state.lookup(ins.a), state.lookup(ins.b));
          if (state.exprs.containsKey(e)) {
            state.rename.put(ins.dest, state.exprs.get(e));
          } else {
            Value rename = new Value();
            state.exprs.put(e, rename);
            state.rename.put(ins.dest, rename);
          }
          break;
        case CMP:
        case TEST:
          e = new ExprDesc(ins.op, state.lookup(ins.a), state.lookup(ins.b));
          if (!state.exprs.containsKey(e)) {
            Value rename = Value.dummy;
            state.exprs.put(e, rename);
          }
          break;
        case FAKE_DIV:
          e = new ExprDesc(ins.op, state.lookup(ins.a), state.lookup(ins.b));
          if (state.exprs.containsKey(e)) {
            if (ins.dest != Value.dummy) {
              state.rename.put(ins.dest, ((DivRes) state.exprs.get(e)).q);
            }
            if (ins.dest2() != Value.dummy) {
              state.rename.put(ins.dest2(), ((DivRes) state.exprs.get(e)).r);
            }
          } else {
            Value renameq = new Value();
            Value renamer = new Value();
            state.exprs.put(e, new DivRes(renameq, renamer));
            if (ins.dest != Value.dummy) {
              state.rename.put(ins.dest, renameq);
            }
            if (ins.dest2() != Value.dummy) {
              state.rename.put(ins.dest2(), renamer);
            }
          }
          break;
        }
      }
    }
    return state;
  }

  @Override
  public void transform(BasicBlock b) {
    State in = (State) get(b).in.clone();
    State out = (State) get(b).out;

    for (int i = 0; i < b.size();i++) {
      Instruction ins = b.get(i);
      if (ins.op.isAnnotation()) {
        continue;
      }

      if (ins.dest instanceof BSSObject) {
        globals.add(ins.dest);
      }

      if (ins.a instanceof BSSObject) {
        globals.add(ins.a);
      }

      if (ins.b instanceof BSSObject) {
        globals.add(ins.b);
      }

      if (ins.op == Op.FAKE_CALL) {
        for (Operand op : globals) {
          in.invalidate(op);
        }
      }

      if (ins.op.invalidateFlag()) {
        for (Iterator<Entry<ExprDesc, Operand>> it = in.exprs.entrySet().iterator(); it.hasNext();) {
          Op op = it.next().getKey().op;
          if (op == Op.CMP || op == Op.TEST) {
            it.remove();
          }
        }
      }

      for (Operand dest : ins.getDestOperand()) {
        in.invalidate(dest);
      }

      if (!ins.twoOperand) {
        boolean fresh = false, freshcmp = false;

        switch (ins.op) {
        case ADD:
        case SUB:
        case IMUL:
        case XOR:
        case SAL:
        case SAR:
        case NOT:
        case NEG:
        case INC:
        case DEC:
        case LOAD:
          ExprDesc e = new ExprDesc(ins.op, in.lookup(ins.a), in.lookup(ins.b));
          if (in.exprs.containsKey(e)) {
            in.rename.put(ins.dest, in.exprs.get(e));
            b.set(i, new Instruction(ins.dest, Op.MOV, in.exprs.get(e)));
          } else {
            Operand rename;
            if (out.exprs.containsKey(e)) {
              rename = out.exprs.get(e);
            } else {
              rename = new Value();
            }
            in.exprs.put(e, rename);
            in.rename.put(ins.dest, rename);
            b.add(++i, new Instruction(rename, Op.MOV, ins.dest));
          }
          break;
        case CMP:
        case TEST:
          e = new ExprDesc(ins.op, in.lookup(ins.a), in.lookup(ins.b));
          if (in.exprs.containsKey(e)) {
            b.set(i, new Instruction(Op.DELETED));
          } else {
            Value rename = Value.dummy;
            in.exprs.put(e, rename);
          }
          break;
        case FAKE_DIV:
          e = new ExprDesc(ins.op, in.lookup(ins.a), in.lookup(ins.b));
          if (in.exprs.containsKey(e)) {
            b.set(i, new Instruction(Op.DELETED));
            if (ins.dest != Value.dummy) {
              in.rename.put(ins.dest, ((DivRes) in.exprs.get(e)).q);
              b.add(++i, new Instruction(ins.dest, Op.MOV, ((DivRes) in.exprs.get(e)).q));
            }
            if (ins.dest2() != Value.dummy) {
              in.rename.put(ins.dest2(), ((DivRes) in.exprs.get(e)).r);
              b.add(++i, new Instruction(ins.dest2(), Op.MOV, ((DivRes) in.exprs.get(e)).r));
            }
          } else {
            Operand renameq, renamer;
            if (out.exprs.containsKey(e)) {
              DivRes temp = ((DivRes) out.exprs.get(e));
              renameq = temp.q;
              renamer = temp.r;
            } else {
              renameq = new Value();
              renamer = new Value();
            }
            in.exprs.put(e, new DivRes(renameq, renamer));
            if (ins.dest != Value.dummy) {
              in.rename.put(ins.dest, renameq);
              if (ins.dest2() != Value.dummy) {
                in.rename.put(ins.dest2(), renamer);
                b.add(++i, new Instruction(renameq, Op.MOV, ins.dest));
                b.add(++i, new Instruction(renamer, Op.MOV, ins.dest2()));
              } else {
                b.set(i, new Instruction.DivInstruction(ins.dest, renamer, in.lookup(ins.a), in.lookup(ins.b)));
                b.add(++i, new Instruction(renameq, Op.MOV, ins.dest));
              }
            } else {
              if (ins.dest2() != Value.dummy) {
                in.rename.put(ins.dest2(), renamer);
                b.set(i, new Instruction.DivInstruction(renameq, ins.dest2(), in.lookup(ins.a), in.lookup(ins.b)));
                b.add(++i, new Instruction(renamer, Op.MOV, ins.dest2()));
              } else {
                assert (false);
              }
            }
          }
          break;
        }
      }
    }
  }

  @Override
  public void reset() {
    super.reset();
    globals = new HashSet<>();
  }

}

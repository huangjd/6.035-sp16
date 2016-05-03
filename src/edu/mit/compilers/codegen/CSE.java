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
      ArrayList<AbstractMap.Entry<ExprDesc, Operand>> xx = new ArrayList<>();
      for (Iterator<AbstractMap.Entry<ExprDesc, Operand>> it = exprs.entrySet().iterator(); it.hasNext();) {
        AbstractMap.Entry<ExprDesc, Operand> ent = it.next();
        ExprDesc e = ent.getKey();
        if (old.equals(e.a) || old.equals(e.b)) {
          xx.add(ent);
          it.remove();
        }
      }
      for (AbstractMap.Entry<ExprDesc, Operand> ent : xx) {
        ExprDesc e = ent.getKey();
        if (old.equals(e.a)) {
          e.a = n;
        }
        if (old.equals(e.b)) {
          e.b = n;
        }
        exprs.put(e, ent.getValue());
      }

      for (Operand o : rename.keySet()) {
        if (rename.get(o).equals(old)) {
          rename.put(o, n);
        }
      }

      Operand o2 = rename.get(old);
      if (o2 != null) {
        rename.remove(old);
        rename.put(n, o2);
      }
    }

    @Override
    public State merge(BasicBlockAnalyzeTransformPass.State t) {
      State other = (State) t;

      HashMap<ExprDesc, Operand> newexpr = new HashMap<>();
      HashMap<Operand, Operand> newrename = new HashMap<>();

      ArrayList<AbstractMap.Entry<Operand, Operand>> replaceList = new ArrayList<>();
      for (AbstractMap.Entry<ExprDesc, Operand> e : exprs.entrySet()) {
        ExprDesc e1 = e.getKey();
        Operand o1 = e.getValue();
        Operand o2 = other.exprs.get(e1);
        if (o2 != null) {
          if (!o1.equals(o2)) {
            if (o1 instanceof DivRes) {
              replaceList.add(new AbstractMap.SimpleEntry<>(((DivRes) o1).q, ((DivRes) o2).q));
              replaceList.add(new AbstractMap.SimpleEntry<>(((DivRes) o1).r, ((DivRes) o2).r));
            } else if (o1 != Value.dummy) {
              replaceList.add(new AbstractMap.SimpleEntry<>(o1, o2));
            }
          }
          newexpr.put(e1, o2);
        }
      }

      for (AbstractMap.Entry<Operand, Operand> ent : replaceList) {
        replaceOut(ent.getKey(), ent.getValue());
      }

      for (AbstractMap.Entry<Operand, Operand> e : rename.entrySet()) {
        Operand k1 = e.getKey();
        Operand v1 = e.getValue();
        Operand v2 = other.rename.get(k1);
        if (v1.equals(v2)) {
          newrename.put(k1, v1);
        }
      }

      this.exprs = (HashMap<ExprDesc, Operand>) newexpr.clone();
      this.rename = (HashMap<Operand, Operand>) newrename.clone();
      other.exprs = (HashMap<ExprDesc, Operand>) newexpr.clone();
      other.rename = (HashMap<Operand, Operand>) newrename.clone();

      return new State(newexpr, newrename);
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
      for (Iterator<AbstractMap.Entry<ExprDesc, Operand>> it = exprs.entrySet().iterator(); it.hasNext();) {
        ExprDesc desc = it.next().getKey();
        if (op.equals(desc.a) || op.equals(desc.b)) {
          it.remove();
        }
      }
    }

    void invalidateExcept(Operand op, ExprDesc e) {
      for (Iterator<AbstractMap.Entry<ExprDesc, Operand>> it = exprs.entrySet().iterator(); it.hasNext();) {
        ExprDesc desc = it.next().getKey();
        if (!desc.equals(e)) {
          if (op.equals(desc.a) || op.equals(desc.b)) {
            it.remove();
          }
        }
      }
    }
  }

  @Override
  public State getInitValue() {
    return new State(new HashMap<ExprDesc, Operand>(), new HashMap<Operand, Operand>());
  }

  boolean notSelfAssign(Instruction ins) {
    if (ins.dest.equals(ins.a) || ins.dest.equals(ins.b)) {
      return false;
    }
    if (ins.dest2() != null) {
      if (ins.dest2().equals(ins.a) || ins.dest2().equals(ins.b)) {
        return false;
      }
    }
    return true;
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

      if (ins.dest2() instanceof BSSObject) {
        globals.add(ins.dest2());
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
        for (Iterator<AbstractMap.Entry<ExprDesc, Operand>> it = state.exprs.entrySet().iterator(); it.hasNext();) {
          Op op = it.next().getKey().op;
          if (op == Op.CMP || op == Op.TEST) {
            it.remove();
          }
        }
      }
      ExprDesc e = null;

      if (!ins.twoOperand) {
        boolean fresh = false;

        if (notSelfAssign(ins)) {
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
            e = new ExprDesc(ins.op, state.lookup(ins.a), state.lookup(ins.b));
            if (state.exprs.containsKey(e)) {
              state.rename.put(ins.dest, state.exprs.get(e));
            } else {
              Value rename = new Value();
              state.exprs.put(e, rename);
              state.rename.put(ins.dest, rename);
            }
            fresh = true;
            break;
          case CMP:
          case TEST:
          case RANGE:
            e = new ExprDesc(ins.op, state.lookup(ins.a), state.lookup(ins.b));
            if (!state.exprs.containsKey(e)) {
              Value rename = Value.dummy;
              state.exprs.put(e, rename);
            }
            fresh = true;
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
            fresh = true;
            break;
          }
        }
        if (!fresh) {
          for (Operand dest : ins.getDestOperand()) {
            state.invalidate(dest);
          }
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

      if (ins.dest2() instanceof BSSObject) {
        globals.add(ins.dest2());
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
        for (Iterator<AbstractMap.Entry<ExprDesc, Operand>> it = in.exprs.entrySet().iterator(); it.hasNext();) {
          Op op = it.next().getKey().op;
          if (op == Op.CMP || op == Op.TEST) {
            it.remove();
          }
        }
      }
      ExprDesc e = null;

      if (!ins.twoOperand) {
        boolean fresh = false;

        if (notSelfAssign(ins)) {
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
            e= new ExprDesc(ins.op, in.lookup(ins.a), in.lookup(ins.b));
            if (in.exprs.containsKey(e)) {
              in.rename.put(ins.dest, in.exprs.get(e));
              b.set(i, new Instruction(ins.dest, Op.MOV, in.exprs.get(e)));
              if (out.exprs.containsKey(ins.dest)) {
                // b.add(++i, new Instruction(out.exprs.get(ins.dest), Op.MOV,
                // ins.dest));
              }
            } else {
              Operand rename;
              if (out.exprs.containsKey(e)) {
                rename = out.exprs.get(e);
              } else if (out.rename.containsKey(ins.dest)) {
                rename = out.rename.get(ins.dest);
              } else {
                rename = new Value();
              }
              in.exprs.put(e, rename);
              in.rename.put(ins.dest, rename);


              b.add(++i, new Instruction(rename, Op.MOV, ins.dest));
            }
            fresh = true;
            break;
          case CMP:
          case TEST:
          case RANGE:
            e = new ExprDesc(ins.op, in.lookup(ins.a), in.lookup(ins.b));
            if (in.exprs.containsKey(e)) {
              b.set(i, new Instruction(Op.DELETED));
            } else {
              Value rename = Value.dummy;
              in.exprs.put(e, rename);

            }
            fresh = true;
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
              if (out.exprs.containsKey(ins.dest)) {
                // b.add(++i, new Instruction(out.exprs.get(ins.dest), Op.MOV,
                // ins.dest));
              }
              if (out.exprs.containsKey(ins.dest2())) {
                // b.add(++i, new Instruction(out.exprs.get(ins.dest2()), Op.MOV,
                // ins.dest2()));
              }
            } else {
              Operand renameq, renamer;
              if (out.exprs.containsKey(e)) {
                DivRes temp = ((DivRes) out.exprs.get(e));
                renameq = temp.q;
                renamer = temp.r;
              } else {
                if (out.rename.containsKey(ins.dest)) {
                  renameq = out.rename.get(ins.dest);
                } else {
                  renameq = new Value();
                }
                if (out.rename.containsKey(ins.dest2())) {
                  renamer = out.rename.get(ins.dest2());
                } else {
                  renamer = new Value();
                }
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
            fresh = true;
            break;
          }
        }


        if (!fresh) {
          for (Operand dest : ins.getDestOperand()) {
            in.invalidate(dest);
          }
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

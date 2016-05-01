package edu.mit.compilers.codegen;

import java.util.*;

import edu.mit.compilers.codegen.Instruction.DivInstruction;

public class CSE extends BasicBlockAnalyzeTransformPass {

  class ExprDesc {

    Instruction ins;

    ExprDesc(Instruction ins) {
      assert (ins.a != null);
      this.ins = ins;
    }
    @Override
    public boolean equals(Object arg0) {
      if (!(arg0 instanceof ExprDesc)) {
        return false;
      }
      Instruction ins2 = ((ExprDesc) arg0).ins;
      if (ins.op != ins2.op) {
        return false;
      }
      if (ins.op.communicative()) {
        return ins.a.equals(ins2.a) && ins.b.equals(ins2.b) ||
            ins.b.equals(ins2.a) && ins.a.equals(ins2.b);
      }

      return ins2.a != null && ins.a.equals(ins2.a) &&
          (ins.b == null && ins2.b == null || ins.b.equals(ins2.b));
    }

    @Override
    public int hashCode() {
      return 0;
    }
  }

  class State extends BasicBlockAnalyzeTransformPass.State {

    HashSet<ExprDesc> exprs;

    State(HashSet<ExprDesc> exprs) {
      this.exprs = exprs;
    }

    @Override
    public State transform(BasicBlockAnalyzeTransformPass.State t) {
      HashSet<ExprDesc> exprs2 = ((State) t).exprs;
      HashSet<ExprDesc> newExpr = new HashSet<>();
      for (ExprDesc expr : exprs) {
        if (exprs2.contains(expr)) {
          newExpr.add(expr);
        }
      }
      return new State(newExpr);
    }

    @Override
    protected State clone() {
      return new State((HashSet<ExprDesc>) exprs.clone());
    }

    @Override
    public boolean equals(Object arg0) {
      if (!(arg0 instanceof State)) {
        return false;
      }
      HashSet<ExprDesc> exprs2 = ((State) arg0).exprs;

      if (exprs.size() != exprs2.size()) {
        return false;
      }
      for (ExprDesc expr : exprs) {
        if (!exprs2.contains(expr)) {
          return false;
        }
      }
      return true;
    }
  }

  @Override
  public State getInitValue() {
    return new State(new HashSet<ExprDesc>());
  }

  public void evict(HashSet<ExprDesc> set, Operand victim) {
    for (Iterator<ExprDesc> it = set.iterator(); it.hasNext();) {
      ExprDesc next = it.next();
      if (next.ins.dest.equals(victim)) {
        it.remove();
      } else if (next.ins.a != null && next.ins.a.equals(victim)) {
        it.remove();
      } else if (next.ins.b != null && next.ins.b.equals(victim)) {
        it.remove();
      }
    }
  }

  @Override
  public State analyze(BasicBlock b, BasicBlockAnalyzeTransformPass.State in) {
    HashSet<ExprDesc> out = (HashSet<ExprDesc>) ((State) in).exprs.clone();
    for (Instruction ins : b) {
      if (!ins.twoOperand) {
        ExprDesc expr = null;
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
        case CMP:
        case TEST:
        case FAKE_DIV:
        case LOAD:
          expr = new ExprDesc(ins);
          if (!out.contains(expr) && !(ins.a instanceof BSSObject) && !(ins.b instanceof BSSObject)) {
            out.add(expr);
          }
          break;
        }

        if (ins.op.invalidateFlag()) {
          for (Iterator<ExprDesc> it = out.iterator(); it.hasNext();) {
            ExprDesc next = it.next();
            if (next.ins.op == Op.TEST || next.ins.op == Op.CMP) {
              it.remove();
            }

            // if (next.ins.op.testFlag() && Util.implies(expr != null,
            // !expr.equals(next))) {
            // it.remove();
            // }
          }
        }

        if (ins.dest != Value.dummy) {
          evict(out, ins.dest);
        }

        if (ins instanceof DivInstruction) {
          Operand dest2 = ((DivInstruction) ins).dest2;
          if (dest2 != Value.dummy) {
            evict(out, dest2);
          }
        }
      }
    }
    return new State(out);
  }

  ExprDesc get(HashSet<ExprDesc> out, ExprDesc e) {
    for (ExprDesc expr : out) {
      if (expr.equals(e)) {
        return expr;
      }
    }
    return null;
  }

  @Override
  public void transform(BasicBlock b) {
    HashSet<ExprDesc> out  = (HashSet<ExprDesc>) ((State) get(b).in).exprs.clone();

    for (int i = 0; i < b.size();i++) {
      Instruction ins = b.get(i);
      if (!ins.twoOperand) {
        ExprDesc expr = null;
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
          expr = new ExprDesc(ins);
          ExprDesc replacer = get(out, expr);
          if (replacer != null) {
            b.set(i, new Instruction(ins.dest, Op.MOV, replacer.ins.dest));
          } else if (!(ins.a instanceof BSSObject) && !(ins.b instanceof BSSObject)) {
            out.add(expr);
          }
          break;
        case CMP:
        case TEST:
          expr = new ExprDesc(ins);
          replacer = get(out, expr);
          if (replacer != null) {
            b.set(i, new Instruction(Op.DELETED));
          } else if (!(ins.a instanceof BSSObject) && !(ins.b instanceof BSSObject)) {
            out.add(expr);
          }
          break;
        case FAKE_DIV:
          expr = new ExprDesc(ins);
          replacer = get(out, expr);
          if (replacer != null) {
            DivInstruction oldDiv = ((DivInstruction) replacer.ins);
            DivInstruction newDiv = ((DivInstruction) ins);
            if (oldDiv.dest == Value.dummy && newDiv.dest != Value.dummy) {
              oldDiv.dest = newDiv.dest;
            }
            if (oldDiv.dest2 == Value.dummy && newDiv.dest2 != Value.dummy) {
              oldDiv.dest2 = newDiv.dest2;
            }

            if (newDiv.dest != Value.dummy) {
              b.set(i, new Instruction(newDiv.dest, Op.MOV, oldDiv.dest));
              if (newDiv.dest2 != Value.dummy) {
                b.add(++i, new Instruction(newDiv.dest2, Op.MOV, oldDiv.dest2));
              }
            } else {
              if (newDiv.dest2 != Value.dummy) {
                b.set(i, new Instruction(newDiv.dest2, Op.MOV, oldDiv.dest2));
              }
            }
          } else if (!(ins.a instanceof BSSObject) && !(ins.b instanceof BSSObject)) {
            out.add(expr);
          }
          break;
        }

        if (ins.op.invalidateFlag()) {
          for (Iterator<ExprDesc> it = out.iterator(); it.hasNext();) {
            ExprDesc next = it.next();
            if (next.ins.op == Op.TEST || next.ins.op == Op.CMP) {
              it.remove();
            }
          }
        }

        if (ins.dest != Value.dummy) {
          evict(out, ins.dest);
        }

        if (ins instanceof DivInstruction) {
          Operand dest2 = ((DivInstruction) ins).dest2;
          if (dest2 != Value.dummy) {
            evict(out, dest2);
          }
        }
      }
    }
  }


}




























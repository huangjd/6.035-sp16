package edu.mit.compilers.codegen;

import java.util.*;

import edu.mit.compilers.codegen.Instruction.CallInstruction;

public class CP extends BasicBlockAnalyzeTransformPass {

  HashSet<Operand> globals;

  class State extends BasicBlockAnalyzeTransformPass.State {

    HashMap<Operand, Operand> rename;

    State(HashMap<Operand, Operand> rename) {
      this.rename = rename;
    }

    @Override
    public State merge(BasicBlockAnalyzeTransformPass.State t) {
      State other = (State) t;

      HashMap<Operand, Operand> map = new HashMap<>();

      /*for (Entry<Operand, Operand> e : rename.entrySet()) {
        if (e.getValue().equals(other.rename.get(e.getKey()))) {
          map.put(e.getKey(), e.getValue());
        }
      }*/

      //this.rename = (HashMap<Operand, Operand>) map.clone();
      //other.rename = (HashMap<Operand, Operand>) map.clone();

      map = (HashMap<Operand, Operand>) rename.clone();
      for (Entry<Operand, Operand > e2 : other.rename.entrySet()) {
        map.put(e2.getKey(), e2.getValue());
      }

      return new State(map);
    }

    @Override
    protected State clone() {
      return new State((HashMap<Operand, Operand>) rename.clone());
    }

    @Override
    public boolean equals(Object arg0) {
      if (!(arg0 instanceof State)) {
        return false;
      }
      State other = (State) arg0;
      if (rename.size() != other.rename.size()) {
        return false;
      }
      for (Operand op : rename.keySet()) {
        Operand op1 = rename.get(op);
        Operand op2 = other.rename.get(op);
        if (!op1.equals(op2)) {
          return false;
        }
      }
      return true;
    }

    Operand getOrDefault(Operand op) {
      Operand op2 = rename.get(op);
      return op2 == null ? op : op2;
    }

    void invalidate(Operand op) {
      rename.remove(op);
      for (Iterator<Entry<Operand, Operand>> it = rename.entrySet().iterator(); it.hasNext();) {
        Entry<Operand, Operand> e = it.next();
        if (e.getValue().equals(op)) {
          it.remove();
        }
      }
    }

    void put(Operand k, Operand v) {
      rename.put(k, getOrDefault(v));
    }
  }

  @Override
  public State getInitValue() {
    return new State(new HashMap<Operand, Operand>());
  }

  @Override
  public State analyze(BasicBlock b, BasicBlockAnalyzeTransformPass.State in) {
    State out = ((State) in).clone();
    for (Instruction ins : b) {
      if (ins.op.isAnnotation()) {
        continue;
      }

      if (!ins.twoOperand) {
        if (ins.op == Op.MOV && ins.a instanceof Value && ins.dest instanceof Value) {
          out.put(ins.dest, ins.a);
        } else {
          for (Operand dest : ins.getDestOperand()) {
            out.invalidate(dest);
          }
        }
      }
    }
    return out;
  }

  @Override
  public void transform(BasicBlock b) {
    State in = (State) get(b).in;
    State out = (State) get(b).out;
    for (int i = 0; i < b.size(); i++) {
      Instruction ins = b.get(i);
      if (ins.op == Op.MOV && ins.a instanceof Value && ins.dest instanceof Value) {
        in.put(ins.dest, ins.a);
        if (out.rename.containsKey(ins.dest)) {
          b.set(i, new Instruction(Op.DELETED));
        }
      } else {
        ins.a = in.getOrDefault(ins.a);
        ins.b = in.getOrDefault(ins.b);

        switch (ins.op) {
        case FAKE_CALL:
          CallInstruction call = (CallInstruction) ins;
          for (int j = 0; j < call.args.size(); j++) {
            call.args.set(j, in.getOrDefault(call.args.get(j)));
          }
          break;
        case STORE:
          ins.dest = in.getOrDefault(ins.dest);
        }

        for (Operand dest : ins.getDestOperand()) {
          in.invalidate(dest);
        }
      }
    }
  }

  /*
   * @Override
   * public void transform(BasicBlock b) {
   * State in = (State) (get(b).in);
   * State out = (State) (get(b).out);
   * int call_count = 0;
   * for (int i = 0; i < b.size(); i++) {
   * Instruction ins = b.get(i);
   * if (ins.op.isAnnotation()) {
   * continue;
   * }
   *
   * if (ins.dest instanceof BSSObject) {
   * globals.add(ins.dest);
   * }
   *
   * if (ins.a instanceof BSSObject) {
   * globals.add(ins.a);
   * }
   *
   * if (ins.b instanceof BSSObject) {
   * globals.add(ins.b);
   * }
   *
   * if (ins.op == Op.FAKE_CALL) {
   * for (Operand op : globals) {
   * in.rename.remove(op);
   * }
   * call_count++;
   * }
   *
   * Operand op;
   * if ((op = in.rename.get(ins.a)) != null) {
   * ins.a = op;
   * }
   * if ((op = in.rename.get(ins.b)) != null) {
   * ins.b = op;
   * }
   * if (ins.op == Op.FAKE_CALL) {
   * ArrayList<Operand> args = ((CallInstruction) ins).args;
   * for (int j = 0; j < args.size(); j++) {
   * if ((op = in.rename.get(args.get(j))) != null) {
   * args.set(j, op);
   * }
   * }
   * }
   *
   * if (!ins.twoOperand) {
   * Operand o1 = in.rename.get(ins.a);
   * Operand o2 = in.rename.get(ins.b);
   *
   * if (o1 == null) {
   * o1 = ins.a;
   * }
   * if (o2 == null) {
   * o2 = ins.b;
   * }
   *
   * if (ins.op == Op.MOV) {
   * in.rename.put(ins.dest, o1);
   * if (o1.equals(out.rename.get(ins.dest)) && !(o1.isImm())) {
   * b.set(i, new Instruction(Op.DELETED));
   * }
   * } else {
   * boolean fresh = false;
   * if (o1 instanceof Imm64 || o1 instanceof Imm8) {
   * long x = o1 instanceof Imm8 ? ((Imm8) o1).val : ((Imm64) o1).val;
   * long z = 0, z2 = 0;
   * boolean ok = true;
   * if (o2 instanceof Imm64 || o2 instanceof Imm8) {
   * long y = o2 instanceof Imm8 ? ((Imm8) o2).val : ((Imm64) o2).val;
   * switch (ins.op) {
   * case ADD:
   * z = y + x;
   * break;
   * case SUB:
   * z = y - x;
   * break;
   * case IMUL:
   * z = y * x;
   * break;
   * case XOR:
   * z = y ^ x;
   * break;
   * case SAL:
   * z = y << x;
   * break;
   * case SAR:
   * z = y >> x;
   * break;
   * case FAKE_DIV:
   * if (y != 0) {
   * z = x / y;
   * z2 = x % y;
   * if (ins.dest2() != Value.dummy) {
   * in.rename.put(ins.dest2(), new Imm64(z2));
   * b.add(i++, new Instruction(ins.dest2(), Op.MOV, new Imm64(z2)));
   * }
   * } else {
   * ok = false;
   * }
   * case RANGE:
   * z = x;
   * if (z >= 0 && z < y) {
   * b.set(i, new Instruction(Op.DELETED));
   * } else {
   * b.set(i, new Instruction(Op.JMP, new Symbol("_exit.0")));
   * }
   * break;
   * default:
   * ok = false;
   * }
   * if (ok) {
   * fresh = true;
   * if (ins.dest != Value.dummy) {
   * in.rename.put(ins.dest, o1 instanceof Imm8 ? new Imm8((byte) z) : new
   * Imm64(z));
   * b.set(i, new Instruction(ins.dest, Op.MOV, o1 instanceof Imm8 ? new
   * Imm8((byte) z) : new Imm64(z)));
   * }
   * }
   * } else {
   * switch (ins.op) {
   * case NOT:
   * z = x ^ 1l;
   * break;
   * case NEG:
   * z = -x;
   * break;
   * case INC:
   * z = x + 1;
   * break;
   * case DEC:
   * z = x - 1;
   * break;
   * default:
   * ok = false;
   * }
   * if (ok) {
   * fresh = true;
   * if (ins.dest != Value.dummy) {
   * in.rename.put(ins.dest, o1 instanceof Imm8 ? new Imm8((byte) z) : new
   * Imm64(z));
   * b.set(i, new Instruction(ins.dest, Op.MOV, o1 instanceof Imm8 ? new
   * Imm8((byte) z) : new Imm64(z)));
   * }
   * }
   * }
   * }
   * if (!fresh) {
   * if (ins.dest != Value.dummy) {
   * in.rename.remove(ins.dest);
   * in.rename.put(ins.dest, ins.dest);
   * }
   * if (ins.dest2() != null && ins.dest2() != Value.dummy) {
   * in.rename.remove(ins.dest2());
   * in.rename.put(ins.dest2(), ins.dest2());
   * }
   * }
   * }
   * }
   * }
   * }
   */

  @Override
  public void reset() {
    super.reset();
    globals = new HashSet<>();
  }

}

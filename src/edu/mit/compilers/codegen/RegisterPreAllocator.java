package edu.mit.compilers.codegen;

import java.util.HashMap;

public class RegisterPreAllocator extends BasicBlockVisitor<RegisterPreAllocator.State> {

  static final int stage = 3;

  boolean omitrbp = false;

  public static class State implements Transformable<State> {
    final int val;
    final HashMap<Operand, Integer> renameTable;

    State(int val) {
      this.val = val;
      renameTable = new HashMap<>();
    }

    State(int val, HashMap<Operand, Integer> map) {
      this.val = val;
      renameTable = map;
    }

    @Override
    public State transform(State t) {
      return t;
    }

    @Override
    public boolean equals(Object arg0) {
      return arg0 != null;
    }
  }

  public RegisterPreAllocator(boolean omitrbp) {
    this.omitrbp = omitrbp;
  }

  @Override
  public State getInitValue() {
    int regs = Register.calleeSavedRegs | Register.callerSavedRegs;
    regs |= (1 << Register.r10.id);
    regs |= (1 << Register.r11.id);
    regs &= ~(1 << Register.rax.id);
    regs &= ~(1 << Register.rdx.id);
    regs &= ~(1 << Register.rsp.id);
    if (omitrbp) {
      regs &= ~(1 << Register.rbp.id);
    }
    return new State(regs);
  }

  @Override
  public State visit(BasicBlock b, State in) {
    int val = in.val;
    HashMap<Operand, Integer> renameTable = (HashMap) in.renameTable.clone();
    for (int i = 0; i < b.size(); i++) {
      Instruction ins = b.get(i);
      assert (ins.twoOperand || ins.op.stage() >= stage);

      if (ins.op == Op.LOOP_START) {
        Integer old = renameTable.get(ins.dest);
        if (old != null) {
          int depth = old >>> 4;
      depth++;
      old = old & (0xF) | (depth << 4);
      renameTable.put(ins.dest, old);
        } else {
          int avail = Integer.numberOfTrailingZeros(val);
          if (avail < 16) {
            Register reg = Register.regs[avail];
            val &= ~(1 << avail);
            b.add(i++, new Instruction(reg, Op.MOV, ins.dest));
            renameTable.put(ins.dest, avail);
          }
        }
      } else if (ins.op == Op.LOOP_END) {
        Integer old = renameTable.get(ins.dest);
        if (old != null) {
          int depth = old >>> 4;
            if (depth == 0) {
              val |= 1 << (old & 0xF);
              renameTable.remove(ins.dest);
            b.add(i++, new Instruction(ins.dest, Op.MOV, Register.regs[old & 0xF]));
            } else {
              depth--;
              old = old & (0xF) | (depth << 4);
              renameTable.put(ins.dest, old);
            }
        }
      } else {
        if (ins.dest != null) {
          Integer rename = renameTable.get(ins.dest);
          if (rename != null) {
            ins.dest = Register.regs[rename & 0xF];
          }
        }
        if (ins.a != null) {
          Integer rename = renameTable.get(ins.a);
          if (rename != null) {
            ins.a = Register.regs[rename & 0xF];
          }
        }
        if (ins.b != null) {
          Integer rename = renameTable.get(ins.b);
          if (rename != null) {
            ins.b = Register.regs[rename & 0xF];
          }
        }
      }
    }
    return new State(val, renameTable);
  }
}

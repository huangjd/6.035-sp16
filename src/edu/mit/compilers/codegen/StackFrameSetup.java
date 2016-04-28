package edu.mit.compilers.codegen;

import edu.mit.compilers.common.Util;

public class StackFrameSetup extends BasicBlockAnalyzeTransformPass {

  boolean omitrbp = false;

  int maxrbpoffset;
  int maxallocation;

  static final int stage = 13;

  public static class State extends BasicBlockAnalyzeTransformPass.State {
    @Override
    public State transform(BasicBlockAnalyzeTransformPass.State t) {
      return (State) t;
    }

    @Override
    protected State clone() {
      return this;
    }

    @Override
    public boolean equals(Object arg0) {
      return super.equals(arg0);
    }
  }

  public StackFrameSetup(boolean omitrbp) {
    this.omitrbp = omitrbp;
  }

  @Override
  public State analyze(BasicBlock b, BasicBlockAnalyzeTransformPass.State in) {
    for (Instruction ins : b) {
      assert (ins.twoOperand || ins.op.stage() >= stage);
      if (ins.a instanceof Memory) {
        Memory mem = (Memory) ins.a;
        if (mem.base == Register.orbp) {
          maxrbpoffset = Math.min(maxrbpoffset, mem.offset);
        }
      }
      if (ins.b instanceof Memory) {
        Memory mem = (Memory) ins.b;
        if (mem.base == Register.orbp) {
          maxrbpoffset = Math.min(maxrbpoffset, mem.offset);
        }
      }
      if (ins.op == Op.ALLOCATE) {
        maxallocation = Math.max(maxallocation, (int) ((Imm64) ins.a).val);
      }
    }
    return (State) in;
  }

  @Override
  public void transform(BasicBlock b) {
    int stackoffset = Util.roundUp(-maxrbpoffset + maxallocation, 16);
    for (int i = 0; i < b.size(); i++) {
      Instruction ins = b.get(i);
      if (ins.op == Op.ALLOCATE || ins.op == Op.LOCAL_ARRAY_DECL) {
        b.set(i, new Instruction(Op.DELETED));
      }
      if (omitrbp) {
        if (ins.op == Op.PROLOGUE) {
          b.set(i, new Instruction(Op.SUB, new Imm64(stackoffset), Register.rsp));
        }
        if (ins.op == Op.EPILOGUE) {
          b.set(i, new Instruction(Op.ADD, new Imm64(stackoffset), Register.rsp));
        }
        if (ins.a instanceof Memory) {
          assert (!(ins.b instanceof Memory));
          Memory mem = (Memory) ins.a;
          if (mem.base == Register.orbp) {
            b.set(i, new Instruction(ins.op,
                new Memory(Register.rsp, mem.index, stackoffset - mem.offset, mem.multiplier), ins.b));
          }
        } else if (ins.b instanceof Memory) {
          Memory mem = (Memory) ins.b;
          if (mem.base == Register.orbp) {
            b.set(i, new Instruction(ins.op, ins.a,
                new Memory(Register.rsp, mem.index, stackoffset - mem.offset, mem.multiplier)));
          }
        }
      } else {
        if (ins.op == Op.PROLOGUE) {
          b.set(i, new Instruction(Op.PUSH, Register.rbp));
          b.add(++i, new Instruction(Op.MOV, Register.rsp, Register.rbp));
          b.add(++i, new Instruction(Op.SUB, new Imm64(stackoffset), Register.rsp));
        }
        if (ins.op == Op.EPILOGUE) {
          b.set(i, new Instruction(Op.MOV, Register.rbp, Register.rsp));
          b.add(++i, new Instruction(Op.POP, Register.rbp));
        }
      }
    }
  }

  @Override
  public State getInitValue() {
    return new State();
  }

  @Override
  public void reset() {
    super.reset();
    maxrbpoffset = 0;
    maxallocation = 0;
  }

}

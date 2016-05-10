package edu.mit.compilers.codegen;

import java.util.ArrayList;

import edu.mit.compilers.codegen.Instruction.CallInstruction;
import edu.mit.compilers.codegen.Operand.Type;
import edu.mit.compilers.common.Util;

public class ResolveTempReg extends BasicBlockAnalyzeTransformPass {

  final int stage = 10;
  int level = 0;
  boolean omitrbp = false;

  public ResolveTempReg(boolean omitrbp) {
    this.omitrbp = omitrbp;
  }

  public static class State extends BasicBlockAnalyzeTransformPass.State {
    final int val;

    State(int val) {
      this.val = val;
    }

    @Override
    public State merge(BasicBlockAnalyzeTransformPass.State t) {
      return new State(val | ((State) t).val);
    }

    @Override
    protected State clone() {
      return new State(val);
    }

    @Override
    public boolean equals(Object arg0) {
      return arg0 instanceof State && val == ((State) arg0).val;
    }
  }

  @Override
  public State getInitValue() {
    return new State(Register.calleeSavedRegs);
  }

  static int or(int[] vals) {
    int res = 0;
    for (int val : vals) {
      if (val >= 0) {
        res |= 1 << val;
      }
    }
    return res;
  }

  Register getFreeReg(int i) {
    i |= 1 << Register.rax.id;
    i |= 1 << Register.rsp.id;
    if (!omitrbp) {
      i |= 1 << Register.rbp.id;
    }

    i = ~i;
    int index = Integer.numberOfTrailingZeros(i);
    if (index < 16) {
      return Register.regs[index];
    } else {
      return null;
    }
  }

  @Override
  public State analyze(BasicBlock b, BasicBlockAnalyzeTransformPass.State in) {
    int out = ((State) in).val;
    for (int i = b.size() - 1; i >= 0; i--) {
      Instruction ins = b.get(i);
      assert (ins.twoOperand || ins.op.stage() >= stage);
      int readset = or(Register.regsToIndices(ins.getRegRead()));
      int writeset = or(Register.regsToIndices(ins.getRegWrite()));
      out &= ~writeset;
      out |= readset;
    }
    return new State(out);
  }

  public void transform2(BasicBlock b) {
    int out = ((State) get(b).out).val;
    for (int i = b.size() - 1; i >= 0; i--) {
      Instruction ins = b.get(i);
      assert (ins.twoOperand || ins.op.stage() >= stage);

      int readset = or(Register.regsToIndices(ins.getRegRead()));
      int writeset = or(Register.regsToIndices(ins.getRegWrite()));
      out &= ~writeset;

      switch (ins.op) {
      case FAKE_DIV:
        ArrayList<Instruction> pre = new ArrayList<>();
        ArrayList<Instruction> post = new ArrayList<>();
        Operand quo = ins.dest;
        Operand rem = ins.dest2();

        Operand saverdx = null;
        Operand constreg = null;
        int out2 = out;
        if ((out & (1 << Register.rdx.id)) != 0) {
          saverdx = getFreeReg(~out);
          if (saverdx == null) {
            saverdx = new Memory(Register.rsp, -8, Type.r64);
          } else {
            out2 |= 1 << ((Register) saverdx).id;
          }
          pre.add(new Instruction(Op.MOV, Register.rdx, saverdx));
          post.add(new Instruction(Op.MOV, saverdx, Register.rdx));
        }
        out2 |= 1 << Register.rdx.id;
        if (ins.b.isImm()) {
          constreg = getFreeReg(~out2);
          if (constreg == null) {
            constreg = new Memory(Register.rsp, -16, Type.r64);
            pre.addAll(Instruction.emitMov(ins.b, constreg));
          } else {
            pre.add(new Instruction(Op.MOV, ins.b, constreg));
          }
          ins.b = constreg;
        }
        pre.add(new Instruction(Op.MOV, ins.a, Register.rax));
        pre.add(new Instruction(Op.CQO));
        pre.add(new Instruction(Op.IDIV, constreg));
        if (ins.dest != Value.dummy) {
          pre.add(new Instruction(Op.MOV, Register.rax, ins.dest));
        }
        if (ins.dest2() != Value.dummy) {
          pre.add(new Instruction(Op.MOV, Register.rdx, ins.dest2()));
        }
        pre.addAll(post);
        b.replaceIns(i, pre);
        break;
      case FAKE_CALL:
        pre = new ArrayList<>();
        post = new ArrayList<>();

        CallInstruction call = (CallInstruction) ins;
        ArrayList<Operand> args = (ArrayList<Operand>) call.args.clone();

        int regsToSave = out & Register.callerSavedRegs & ~(1 << Register.rax.id);
        int regsAvail = (0xFFFF)
            & ~(out | (1 << Register.rax.id) | (1 << Register.rbp.id) | (1 << Register.rsp.id)
                | Register.callerSavedRegs);
        int stackNeeded = Math.max(0, Integer.bitCount(regsToSave) - Integer.bitCount(regsAvail)) * 8;
        pre.add(new Instruction(Value.dummy, Op.ALLOCATE, new Imm64(stackNeeded +
            Math.max(0, args.size() - 6) * 8)));

        int count = 0;
        int offset = Math.max(0, args.size() - 6) * 8;
        while (regsToSave != 0) {
          int k = Integer.numberOfTrailingZeros(regsToSave);
          Register savee = Register.regs[k];
          Operand saver;
          if ((regsAvail & 0xFFFF) != 0) {
            int l = Integer.numberOfTrailingZeros(regsAvail);
            saver = Register.regs[l];
            regsAvail &= ~(1 << l);
          } else {
            saver = new Memory(Register.rsp, offset + 8 * (count++), Operand.Type.r64);
          }
          pre.add(new Instruction(Op.MOV, savee, saver));
          post.add(new Instruction(Op.MOV, saver, savee));
          regsToSave &= ~(1 << k);
        }
        assert(offset + 8 * (count) == stackNeeded);
        if (ins.dest != Value.dummy) {
          post.add(new Instruction(Op.MOV, Register.rax, ins.dest));
        }

        for (int j = 6; j < args.size(); j++) {
          if (args.get(j).getType() == Type.r64) {
            pre.addAll(Instruction.emitMov(args.get(j), new Memory(Register.rsp, -(j - 6) * 8, Type.r64)));
          } else {
            pre.add(new Instruction(Op.MOVSX, args.get(j), Register.rax));
            pre.add(new Instruction(Op.MOV, Register.rax, new Memory(Register.rsp, -(j - 6) * 8, Type.r64)));
          }
        }
        ArrayList<Operand> reshuffle = new ArrayList<>();

        for (int j = Math.min(args.size(), 6) - 1; j >= 0; j--) {
          Operand current = args.get(j);
          boolean x = true;
          for (int k = 0; k < Math.min(args.size(), 6); k++) {
            if (k != j && Util.in(args.get(k).getInvolvedRegs(), Register.argsRegs[j])) {
              reshuffle.add(Register.argsRegs[j]);
              if (current.isImm64N32() || current.getType() == Type.r8) {
                pre.add(movx(current, Register.rax));
                pre.add(new Instruction(Op.PUSH, Register.rax));
              } else {
                pre.add(new Instruction(Op.PUSH, current));
              }
              x  = false;
              break;
            }
          }
          if (x) {
            pre.add(movx(current, Register.argsRegs[j]));
          }
        }
        for (int j = reshuffle.size() - 1; j >= 0;j--) {
          pre.add(new Instruction(Op.POP, reshuffle.get(j)));
        }

        if (call.variadic) {
          pre.add(movx(new Imm64(call.variadicXMMArgsCount), Register.rax));
        }

        pre.add(new Instruction(Op.CALL, call.a));
        pre.addAll(post);
        b.replaceIns(i, pre);
        break;
      case GET_ARG:

        break;
      }
      out |= readset;

    }
  }

  static Instruction movx(Operand src, Operand dest) {
    if (src.getType() == Type.r8) {
      return new Instruction(Op.MOVSX, src, dest);
    } else {
      return new Instruction(Op.MOV, src, dest);
    }
  }

  @Override
  public void transform(BasicBlock b) {
    int used = ((State) get(b).out).val;

    for (int i = b.size() - 1; i >= 0; i--) {
      Instruction ins = b.get(i);

      if (ins.twoOperand) {
        int readset = or(Register.regsToIndices(ins.getRegRead()));
        int writeset = or(Register.regsToIndices(ins.getRegWrite()));
        used &= ~writeset;
        used |= readset;
      } else {
        int itmp = i;
        int after = used;
        switch (ins.op) {
        case END_XDIV:
          b.set(itmp, new Instruction(Op.DELETED));
          for (i--;; i--) {
            ins = b.get(i);
            if (ins.twoOperand) {
              int readset = or(Register.regsToIndices(ins.getRegRead()));
              int writeset = or(Register.regsToIndices(ins.getRegWrite()));
              used &= ~writeset;
              used |= readset;
            } else if (ins.op == Op.BEGIN_XDIV) {
              b.set(i, new Instruction(Op.DELETED));
              if ((used & (1 << Register.rdx.id)) != 0) {
                Register reg = getFreeReg(used);
                if (reg != null) {
                  b.set(i, new Instruction(Op.MOV, Register.rdx, reg));
                  b.set(itmp, new Instruction(Op.MOV, reg, Register.rdx));
                } else {
                  b.set(i, new Instruction(Op.MOV, Register.rdx, new Memory(Register.rsp, -8, Operand.Type.r8)));
                  b.set(itmp, new Instruction(Op.MOV, new Memory(Register.rsp, -8, Operand.Type.r8), Register.rdx));
                }
              }
              break;
            } else if (ins.op == Op.END_TEMP_REG) {
              int itmp2 = i;
              int level = 0;
              for (i--;; i--) {
                ins = b.get(i);
                if (ins.twoOperand) {
                  int readset = or(Register.regsToIndices(ins.getRegRead()));
                  int writeset = or(Register.regsToIndices(ins.getRegWrite()));
                  used &= ~writeset;
                  used |= readset;
                } else if (ins.op == Op.TEMP_REG) {
                  b.set(itmp2, new Instruction(Op.DELETED));
                  b.set(i, new Instruction(Op.DELETED));
                  Register reg = getFreeReg(used);
                  if (reg == null) {
                    b.set(i, new Instruction(Op.MOV, Register.rcx, new Memory(Register.rsp, -8, Operand.Type.r8)));
                    b.set(itmp2, new Instruction(Op.MOV, new Memory(Register.rsp, -8, Operand.Type.r8), Register.rcx));
                    level -= 8;
                    reg = Register.rcx;
                  }
                  for (int j = i + 1; j < itmp2; j++) {
                    Instruction ins2 = b.get(j);
                    // if (ins2.a != null && ins2.a.equals(Register.rxx)) {
                    b.set(j, new Instruction(ins2.op, reg, ins2.b));
                    // }
                    ins2 = b.get(j);
                    // if (ins2.b != null && ins2.b.equals(Register.rxx)) {
                    b.set(j, new Instruction(ins2.op, ins2.a, reg));
                    // }
                  }
                  for (i--;; i--) {
                    ins = b.get(i);
                    if (ins.twoOperand) {
                      int readset = or(Register.regsToIndices(ins.getRegRead()));
                      int writeset = or(Register.regsToIndices(ins.getRegWrite()));
                      used &= ~writeset;
                      used |= readset;
                    } else if (ins.op == Op.BEGIN_XDIV) {
                      b.set(i, new Instruction(Op.DELETED));
                      if ((used & (1 << Register.rdx.id)) != 0) {
                        reg = getFreeReg(used | (1 << reg.id));
                        if (reg != null) {
                          b.set(i, new Instruction(Op.MOV, Register.rdx, reg));
                          b.set(itmp, new Instruction(Op.MOV, reg, Register.rdx));
                        } else {
                          b.set(i, new Instruction(Op.MOV, Register.rdx,
                              new Memory(Register.rsp, -8 + level, Operand.Type.r8)));
                          b.set(itmp, new Instruction(Op.MOV, new Memory(Register.rsp, -8 + level, Operand.Type.r8),
                              Register.rdx));
                        }
                      }
                      break;
                    }
                  }
                  break;
                }
              }
              break;
            }
          }
          break;
        case END_TEMP_REG:
          b.set(itmp, new Instruction(Op.DELETED));
          for (i--;; i--) {
            ins = b.get(i);
            if (ins.twoOperand) {
              int readset = or(Register.regsToIndices(ins.getRegRead()));
              int writeset = or(Register.regsToIndices(ins.getRegWrite()));
              used &= ~writeset;
              used |= readset;
            } else if (ins.op == Op.TEMP_REG) {
              b.set(i, new Instruction(Op.DELETED));
              Register reg = getFreeReg(used);
              if (reg == null) {
                b.set(i, new Instruction(Op.MOV, Register.rcx, new Memory(Register.rsp, -8, Operand.Type.r8)));
                b.set(itmp, new Instruction(Op.MOV, new Memory(Register.rsp, -8, Operand.Type.r8), Register.rcx));
                reg = Register.rcx;
              }
              for (int j = i + 1; j < itmp; j++) {
                Instruction ins2 = b.get(j);
                // if (ins2.a != null && ins2.a.equals(Register.rxx)) {
                b.set(j, new Instruction(ins2.op, reg, ins2.b));
                // }
                ins2 = b.get(j);
                // if (ins2.b != null && ins2.b.equals(Register.rxx)) {
                b.set(j, new Instruction(ins2.op, ins2.a, reg));
                // }
              }
              break;
            }
          }
          break;
        case END_XCALL:
          b.set(itmp, new Instruction(Op.DELETED));
          for (i--;; i--) {
            ins = b.get(i);
            if (ins.twoOperand) {
              int readset = or(Register.regsToIndices(ins.getRegRead()));
              int writeset = or(Register.regsToIndices(ins.getRegWrite()));
              used &= ~writeset;
              used |= readset;
            } else if (ins.op == Op.BEGIN_XCALL) {
              b.set(i, new Instruction(Op.DELETED));
              int j;
              long offset;
              Instruction ins2 = null;
              for (j = i - 1;; j--) {
                ins2 = b.get(j);
                if (ins2.op == Op.ALLOCATE) {
                  offset = ((Imm64) ins2.a).val;
                  break;
                }
              }

              int regsToSave = used & Register.callerSavedRegs & ~(1 << Register.rax.id);
              int regsAvail = (0xFFFF)
                  & ~(used | (1 << Register.rax.id) | (1 << Register.rbp.id) | (1 << Register.rsp.id)
                      | Register.callerSavedRegs);

              int stackNeeded = Math.max(0, Integer.bitCount(regsToSave) - Integer.bitCount(regsAvail)) * 8;
              b.set(j, new Instruction(Value.dummy, Op.ALLOCATE, new Imm64(offset + stackNeeded)));

              int count = 0;
              while (regsToSave != 0) {
                int k = Integer.numberOfTrailingZeros(regsToSave);
                Register savee = Register.regs[k];
                Operand saver;
                if ((regsAvail & 0xFFFF) != 0) {
                  int l = Integer.numberOfTrailingZeros(regsAvail);
                  saver = Register.regs[l];
                  regsAvail &= ~(1 << l);
                } else {
                  saver = new Memory(Register.rsp, (int) offset + 8 * (count++), Operand.Type.r64);
                }
                b.add(itmp, new Instruction(Op.MOV, saver, savee));
                b.add(i, new Instruction(Op.MOV, savee, saver));
                itmp++;
                regsToSave &= ~(1 << k);
              }
              break;
            }
          }
          break;
        case BEGIN_XDIV:
        case BEGIN_XCALL:
        case TEMP_REG:
          throw new RuntimeException("Unmatched pair");
        default:
          break;
        }
      }
    }
  }
}

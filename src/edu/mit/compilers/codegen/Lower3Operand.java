package edu.mit.compilers.codegen;

import edu.mit.compilers.codegen.Operand.Type;

public class Lower3Operand extends BasicBlockTraverser {

  public static final int stage = 7;

  @Override
  protected void visit(BasicBlock b) {
    for (int i = 0; i < b.size(); i++) {
      Instruction ins = b.get(i);
      assert (ins.twoOperand || ins.op.stage() >= stage);

      if (ins.op == Op.IDIV) {
        b.set(i, new Instruction(Op.IDIV, ins.a));
      } else if (!ins.twoOperand && ins.op.isa()) {
        if (ins.a == null && ins.b == null) {
          if (ins.dest != Value.dummy) {
            b.set(i, new Instruction(ins.op, ins.dest));
          } else {
            b.set(i, new Instruction(ins.op));
          }
        } else if (ins.a.isImm() && (ins.b == null || ins.b.isImm())) {
          long x = ins.a instanceof Imm8 ? ((Imm8)ins.a).val : ((Imm64)ins.a).val;
          long y = ins.b == null ? 0 : (ins.b instanceof Imm8 ? ((Imm8)ins.b).val : ((Imm64)ins.b).val);
          long z;
          switch (ins.op) {
          case ADD:
            z = y + x;
            break;
          case SUB:
            z = y - x;
            break;
          case IMUL:
            z = y * x;
            break;
          case XOR:
            z = y ^ x;
            break;
          case SAL:
            z = y << x;
            break;
          case SAR:
            z = y >> x;
            break;
          case CMP:
            z = y - x;
            ins.dest = Register.rax;
            b.add(i + 1, new Instruction(Op.TEST, Register.rax, Register.rax));
            break;
          case TEST:
            z = y ^ x;
            ins.dest = Register.rax;
            b.add(i + 1, new Instruction(Op.TEST, Register.rax, Register.rax));
            break;
          case NOT:
            z = x ^ 1l;
            break;
          case NEG:
            z = -x;
            break;
          case INC:
            z = x + 1;
            break;
          case DEC:
            z = x - 1;
            break;
          case MOV:
            z = x;
            break;
          default:
            throw new RuntimeException();
          }
          Operand imm = ins.a instanceof Imm8? new Imm8((byte) z) : new Imm64(z);
          if (ins.dest.isReg() || ins.dest.isMem() && imm.isImm32() || ins.op == Op.MOV) {
            b.set(i, new Instruction(Op.MOV, imm, ins.dest));
          } else {
            b.set(i, new Instruction(Op.MOV, imm, Register.rax));
            b.add(++i, new Instruction(Op.MOV, Register.rax, ins.dest));
          }
        } else if (ins.dest == Value.dummy) {
          if (ins.b.isReg() && !ins.a.isImm64N32() || ins.b.isMem() && (ins.a.isImm32() || ins.a.isReg())) {
            b.set(i, new Instruction(ins.op, ins.a, ins.b));
          } else if (ins.b.isReg() || ins.b.isMem()) {
            b.set(i, new Instruction(Op.MOV, ins.a, Register.rax));
            b.add(++i, new Instruction(ins.op, Register.rax, ins.b));
          } else if (ins.b.isImm32() && ins.op.communicative()) {
            b.set(i, new Instruction(ins.op, ins.b, ins.a));
          } else if (ins.b.isImm()) {
            b.set(i, new Instruction(Op.MOV, ins.b, Register.rax));
            b.add(++i, new Instruction(ins.op, ins.a, Register.rax));
          } else {
            throw new RuntimeException();
          }
        } else if (ins.b == null) {
          if (ins.dest.isMem() && ins.a.isMem()) {
            b.set(i, new Instruction(Op.MOV, ins.a, Register.rax));
            b.add(++i, new Instruction(ins.op, Register.rax, ins.dest));
          } else {
            b.set(i, new Instruction(ins.op, ins.a, ins.dest));
          }
        } else if (ins.dest.isReg()) {
          if (ins.dest.equals(ins.a) && ins.op.communicative() && !ins.b.isImm64N32()) {
            b.set(i, new Instruction(ins.op, ins.b, ins.a));
          } else if (ins.dest.equals(ins.b) && !ins.a.isImm64N32()) {
            b.set(i, new Instruction(ins.op, ins.a, ins.b));
          } else if (ins.a.isMem() && ins.a.equals(ins.b)) {
            b.set(i, new Instruction(Op.MOV, ins.a, ins.dest));
            b.add(++i, new Instruction(ins.op, ins.dest, ins.dest));
          } else if (ins.a.isImm64N32()) {
            if (ins.op.communicative()) {
              b.set(i, new Instruction(Op.MOV, ins.a, ins.dest));
              b.add(++i, new Instruction(ins.op, ins.b, ins.dest));
            } else {
              b.set(i, new Instruction(Op.MOV, ins.a, Register.rax));
              b.add(++i, new Instruction(Op.MOV, ins.b, ins.dest));
              b.add(++i, new Instruction(ins.op, Register.rax, ins.dest));
            }
          } else {
            b.set(i, new Instruction(Op.MOV, ins.b, ins.dest));
            b.add(++i, new Instruction(ins.op, ins.a, ins.dest));
          }
        } else if (ins.dest.isMem()) {
          if (ins.dest.equals(ins.a) && ins.op.communicative()) {
            if (ins.b.isReg() || ins.b.isImm32()) {
              b.set(i, new Instruction(ins.op, ins.b, ins.dest));
            } else {
              b.set(i, new Instruction(Op.MOV, ins.b, Register.rax));
              b.add(++i, new Instruction(ins.op, Register.rax, ins.dest));
            }
          } else if (ins.dest.equals(ins.b)) {
            if (ins.a.isReg() || ins.a.isImm32()) {
              b.set(i, new Instruction(ins.op, ins.a, ins.dest));
            } else {
              b.set(i, new Instruction(Op.MOV, ins.a, Register.rax));
              b.add(++i, new Instruction(ins.op, Register.rax, ins.dest));
            }
          } else if (ins.a.isMem() && ins.a.equals(ins.b)) {
            b.set(i, new Instruction(Op.MOV, ins.a, Register.rax));
            b.add(++i, new Instruction(ins.op, Register.rax, Register.rax));
            b.add(++i, new Instruction(Op.MOV, Register.rax, ins.dest));
          } else if (ins.a.isReg() || ins.a.isImm32()) {
            b.set(i, new Instruction(Op.MOV, ins.b, Register.rax));
            b.add(++i, new Instruction(ins.op, ins.a, Register.rax));
            b.add(++i, new Instruction(Op.MOV, Register.rax, ins.dest));
          } else if (ins.a.isMem()) {
            b.set(i, new Instruction(Op.MOV, ins.b, Register.rax));
            b.add(++i, new Instruction(ins.op, ins.a, Register.rax));
            b.add(++i, new Instruction(Op.MOV, Register.rax, ins.dest));
          } else if (ins.a.isImm64N32()) {
            if (ins.op.communicative()) {
              b.set(i, new Instruction(Op.MOV, ins.a, Register.rax));
              b.add(++i, new Instruction(ins.op, ins.b, Register.rax));
              b.add(++i, new Instruction(Op.MOV, Register.rax, ins.dest));
            } else if (ins.b.isReg() || ins.b.isImm32()) {
              b.add(++i, new Instruction(Op.MOV, ins.b, ins.dest));
              b.set(i, new Instruction(Op.MOV, ins.a, Register.rax));
              b.add(++i, new Instruction(ins.op, Register.rax, ins.dest));
            } else {
              b.set(i, new Instruction(Op.MOV, ins.b, Register.rax));
              b.add(++i, new Instruction(Op.MOV, Register.rax, ins.dest));
              b.add(++i, new Instruction(Op.MOV, ins.a, Register.rax));
              b.add(++i, new Instruction(ins.op, Register.rax, ins.dest));
            }
          } else {
            throw new RuntimeException();
          }
        }
      } else if (ins.op == Op.LOAD) {
        Register base = null, index = null;
        int offset = 0;
        Type type = ins.dest.getType();

        if (ins.a instanceof Memory) {
          base = ((Memory)ins.a).base;
          index = ((Memory)ins.a).index;
          offset = ((Memory) ins.a).offset;
        }

        if (ins.dest.isMem()) {
          b.add(i + 1, new Instruction(Op.MOV, Register.rax, ins.dest));
          ins.dest = Register.rax;
        } else if (!ins.dest.isReg()) {
          throw new RuntimeException("Can load to non reg non mem");
        }

        i--;

        if (ins.b.isReg()) {
          index = (Register) ins.b;
        } else if (ins.b.isMem() || ins.b.isImm64N32()) {
          b.add(++i, new Instruction(Op.MOV, ins.b, Register.rax));
          index = Register.rax;
        } else if (ins.b.isImm32()) {
          offset += ins.b instanceof Imm8 ? ((Imm8) ins.b).val : ((Imm64) ins.b).val * 8;
        } else {
          throw new RuntimeException();
        }

        if (ins.a instanceof Memory && ((Memory) ins.a).index != null) {
          if (ins.b.isMem() || ins.b.isImm64N32()) {
            b.add(++i, new Instruction(Op.ADD, index, Register.rax));
          } else if (ins.b.isReg()) {
            b.add(++i, new Instruction(Op.LEA, ins.a, Register.rax));
            base = Register.rax;
          }
        }

        if (ins.a instanceof Memory) {
          b.set(++i, new Instruction(Op.MOV, new Memory(base, index, offset, type), ins.dest));
        } else if (ins.a instanceof BSSObject) {
          if (index != null) {
            b.set(++i, new Instruction(Op.MOV, new Memory((BSSObject) ins.a, index, type), ins.dest));
          } else {
            b.set(++i, new Instruction(Op.MOV, new Memory((BSSObject) ins.a, offset, type), ins.dest));
          }
        }
      } else if (ins.op == Op.STORE) {
        Register base = null, index = null;
        int offset = 0;
        Type type = ins.dest.getType();

        if (ins.a instanceof Memory) {
          base = ((Memory) ins.a).base;
          index = ((Memory) ins.a).index;
          offset = ((Memory) ins.a).offset;
        }

        i--;

        if (ins.b.isReg()) {
          index = (Register) ins.b;
        } else if (ins.b.isMem() || ins.b.isImm64N32()) {
          b.add(++i, new Instruction(Op.MOV, ins.b, Register.rax));
          index = Register.rax;
        } else if (ins.b.isImm32()) {
          offset += ins.b instanceof Imm8 ? ((Imm8) ins.b).val : ((Imm64) ins.b).val * 8;
        } else {
          throw new RuntimeException();
        }

        if (ins.a instanceof Memory && ((Memory) ins.a).index != null) {
          if (ins.b.isMem() || ins.b.isImm64N32()) {
            b.add(++i, new Instruction(Op.ADD, index, Register.rax));
          } else if (ins.b.isReg()) {
            b.add(++i, new Instruction(Op.LEA, ins.a, Register.rax));
            base = Register.rax;
          }
        }

        boolean needTemp = !(ins.dest.isReg() || ins.dest.isImm32());
        if (needTemp) {
          b.add(++i, new Instruction(Register.rxx, Op.TEMP_REG));
          b.add(++i, new Instruction(Op.MOV, ins.dest, Register.rxx));
          ins.dest = Register.rxx;
        }

        if (ins.a instanceof Memory) {
          b.set(++i, new Instruction(Op.MOV, ins.dest, new Memory(base, index, offset, type)));
        } else if (ins.a instanceof BSSObject) {
          if (index != null) {
            b.set(++i, new Instruction(Op.MOV, ins.dest, new Memory((BSSObject) ins.a, index, type)));
          } else {
            b.set(++i, new Instruction(Op.MOV, ins.dest, new Memory((BSSObject) ins.a, offset, type)));
          }
        }
        if (needTemp) {
          b.add(++i, new Instruction(Value.dummy, Op.END_TEMP_REG));
        }
      }
    }

  }
}

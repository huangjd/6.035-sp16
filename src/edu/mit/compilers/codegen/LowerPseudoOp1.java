package edu.mit.compilers.codegen;

import edu.mit.compilers.codegen.Instruction.*;
import edu.mit.compilers.codegen.Operand.Type;

public class LowerPseudoOp1 extends BasicBlockTraverser {

  @Override
  protected void visit(BasicBlock b) {
    for (int i = 0; i < b.size(); i++) {
      Instruction ins = b.get(i);
      int j = i;
      if (ins instanceof DivInstruction) {
        Operand div = ins.dest;
        Operand mod = ((DivInstruction) ins).dest2;
        b.set(j, new Instruction(Value.dummy, Op.BEGIN_XDIV));

        boolean isImm = ins.b.isImm();

        b.add(++j, new Instruction(Register.rax, Op.MOV, ins.a));
        b.add(++j, new Instruction(Op.CQO));
        if (isImm) {
          b.add(++j, new Instruction(Register.rxx, Op.TEMP_REG));
          b.add(++j, new Instruction(Register.rxx, Op.MOV, ins.b));
          ins.b = Register.rxx;
        }
        b.add(++j, new Instruction(Value.dummy, Op.IDIV, ins.b));
        if (mod != Value.dummy && mod != null) {
          b.add(++j, new Instruction(mod, Op.MOV, Register.rdx));
        }
        if (div != Value.dummy && div != null) {
          b.add(++j, new Instruction(div, Op.MOV, Register.rax));
        }
        if (isImm) {
          b.add(++j, new Instruction(Value.dummy, Op.END_TEMP_REG));
        }
        b.add(++j, new Instruction(Value.dummy, Op.END_XDIV));

      } else if (ins instanceof CallInstruction) {
        CallInstruction call = (CallInstruction) ins;

        boolean nextInsIsTest = b.get(j + 1).op == Op.TEST && b.get(j + 1).a == Register.rax
            && b.get(j + 1).b == Register.rax;

        b.set(j, new Instruction(Value.dummy, Op.BEGIN_XCALL));
        switch (call.args.size()) { // fallthrough
        default:
          for (int k = 6; k < call.args.size(); k++) {
            b.add(++j, new Instruction(new Memory(Register.rsp, (k - 6) * 8, Type.r64), Op.MOV, call.args.get(k)));
          }
        case 6:
          b.add(++j, new Instruction(Op.MOV, call.args.get(5), Register.r9));
          if (call.args.get(5).getType() == Type.r8) {
            b.add(++j, new Instruction(Op.MOVSBQ, Register.r9b, Register.r9));
          }
        case 5:
          b.add(++j, new Instruction(Op.MOV, call.args.get(4), Register.r8));
          if (call.args.get(4).getType() == Type.r8) {
            b.add(++j, new Instruction(Op.MOVSBQ, Register.r8b, Register.r8));
          }
        case 4:
          b.add(++j, new Instruction(Op.MOV, call.args.get(3), Register.rcx));
          if (call.args.get(3).getType() == Type.r8) {
            b.add(++j, new Instruction(Op.MOVSBQ, Register.cl, Register.rcx));
          }
        case 3:
          b.add(++j, new Instruction(Op.MOV, call.args.get(2), Register.rdx));
          if (call.args.get(2).getType() == Type.r8) {
            b.add(++j, new Instruction(Op.MOVSBQ, Register.dl, Register.rdx));
          }
        case 2:
          b.add(++j, new Instruction(Op.MOV, call.args.get(1), Register.rsi));
          if (call.args.get(1).getType() == Type.r8) {
            b.add(++j, new Instruction(Op.MOVSBQ, Register.sil, Register.rsi));
          }
        case 1:
          b.add(++j, new Instruction(Op.MOV, call.args.get(0), Register.rdi));
          if (call.args.get(0).getType() == Type.r8) {
            b.add(++j, new Instruction(Op.MOVSBQ, Register.dil, Register.rdi));
          }
        case 0:
          break;
        }
        if (call.variadic) {
          b.add(++j, new Instruction(Op.MOV, new Imm64(call.variadicXMMArgsCount), Register.rax));
        }
        b.add(++j, new Instruction(Op.CALL, call.a));
        if (ins.dest != Value.dummy) {
          b.add(++j, new Instruction(Op.MOV, Register.rax, ins.dest));
        }
        if (nextInsIsTest) {
          ++j;
        }
        b.add(++j, new Instruction(Value.dummy, Op.END_XCALL));
      } else if (ins.op == Op.GET_ARG) {
        assert (ins.a instanceof Imm64);
        switch ((int) ((Imm64) ins.a).val) {
        case 0:
          b.set(j, new Instruction(Op.MOV, Register.rdi, ins.dest));
          break;
        case 1:
          b.set(j, new Instruction(Op.MOV, Register.rsi, ins.dest));
          break;
        case 2:
          b.set(j, new Instruction(Op.MOV, Register.rdx, ins.dest));
          break;
        case 3:
          b.set(j, new Instruction(Op.MOV, Register.rcx, ins.dest));
          break;
        case 4:
          b.set(j, new Instruction(Op.MOV, Register.r8, ins.dest));
          break;
        case 5:
          b.set(j, new Instruction(Op.MOV, Register.r9, ins.dest));
          break;
        default:
          b.set(j, new Instruction(ins.dest, Op.MOV,
              new Memory(Register.orbp, ((int) ((Imm64) ins.a).val - 4) * 8, Type.r64)));
          break;
        }
      } else if (ins.op.jcc()) {
        b.set(j, new Instruction(ins.op, ins.a));
        b.add(++j, new Instruction(Op.JMP, ins.b));
      } else if (ins.op == Op.JMP) {
        b.set(j, new Instruction(Op.JMP, ins.a));
      } else if (ins.op == Op.RET) {
        b.set(j, new Instruction(Op.RET));
      }
      i = j;
    }
  }

}

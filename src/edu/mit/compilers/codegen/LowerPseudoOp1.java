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

        if (ins.b instanceof Imm64 || ins.b instanceof Imm8) {
          b.add(++j, new Instruction(Op.MOV, ins.b, Register.rcx));
          ins.b = Register.rcx;
        }
        b.add(++j, new Instruction(Op.XOR, Register.rdx, Register.rdx));
        b.add(++j, new Instruction(Op.MOV, ins.a, Register.rax));
        b.add(++j, new Instruction(Op.IDIV, ins.b));
        if (mod != Value.dummy && mod != null) {
          b.add(++j, new Instruction(Op.MOV, Register.rdx, mod));
        }
        if (div != Value.dummy && div != null) {
          b.add(++j, new Instruction(Op.MOV, Register.rax, div));
        }

        b.add(++j, new Instruction(Value.dummy, Op.END_XDIV));

      } else if (ins instanceof CallInstruction) {
        CallInstruction call = (CallInstruction) ins;

        boolean nextInsIsTest = b.get(j + 1).op == Op.TEST && b.get(j + 1).a == Register.rax
            && b.get(j + 1).b == Register.rax;

        b.set(j, new Instruction(Value.dummy, Op.BEGIN_XCALL));
        switch (call.args.size()) { // fallthrough
        default:
          for (int k = 6; k < call.args.size() - 6; k++) {
            b.add(++j, new Instruction(new Memory(Register.rsp, (k - 6) * 8, Type.r64), Op.MOV, call.args.get(k)));
          }
        case 6:
          b.add(++j, new Instruction(Op.MOV, call.args.get(5), Register.r9));
        case 5:
          b.add(++j, new Instruction(Op.MOV, call.args.get(4), Register.r8));
        case 4:
          b.add(++j, new Instruction(Op.MOV, call.args.get(3), Register.rcx));
        case 3:
          b.add(++j, new Instruction(Op.MOV, call.args.get(2), Register.rdx));
        case 2:
          b.add(++j, new Instruction(Op.MOV, call.args.get(1), Register.rsi));
        case 1:
          b.add(++j, new Instruction(Op.MOV, call.args.get(0), Register.rdi));
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
        assert(ins.b instanceof Imm64);
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
              new Memory(Register.orbp, ((int) ((Imm64) ins.b).val - 4) * 8, Type.r64)));
          break;
        }
      } else if (ins.op.jcc()) {
        b.set(j, new Instruction(ins.op, ins.a));
        b.add(++j, new Instruction(Op.JMP, ins.b));
      } else if (ins.op == Op.JMP) {
        b.set(j, new Instruction(Op.JMP, ins.a));
      }
      i = j;
    }
  }

}

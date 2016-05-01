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

        Value saverdx = new Value();
        b.set(j, new Instruction(saverdx, Op.MOV, Register.rdx));
        boolean isImm = ins.b.isImm();

        b.add(++j, new Instruction(Register.rax, Op.MOV, ins.a));
        b.add(++j, new Instruction(Op.CQO));
        if (isImm) {
          Value temp = new Value();
          b.add(++j, new Instruction(temp, Op.MOV, ins.b));
          ins.b = temp;
        }
        b.add(++j, new Instruction(Value.dummy, Op.IDIV, ins.b));
        if (mod != Value.dummy && mod != null) {
          b.add(++j, new Instruction(mod, Op.MOV, Register.rdx));
        }
        if (div != Value.dummy && div != null) {
          b.add(++j, new Instruction(div, Op.MOV, Register.rax));
        }
        b.add(++j, new Instruction(Register.rdx, Op.MOV, saverdx));

      } else if (ins instanceof CallInstruction) {
        CallInstruction call = (CallInstruction) ins;

        boolean nextInsIsTest = b.get(j + 1).op == Op.TEST && b.get(j + 1).a == Register.rax
            && b.get(j + 1).b == Register.rax;

        b.set(j, new Instruction(Value.dummy, Op.BEGIN_XCALL));

        switch (call.args.size()) { // fallthrough
        default:
          for (int k = 6; k < call.args.size(); k++) {
            if (call.args.get(k).getType() == Type.r8) {
              b.add(++j, new Instruction(Register.rax, Op.MOVSX, call.args.get(k)));
              b.add(++j, new Instruction(new Memory(Register.rsp, (k - 6) * 8, Type.r64), Op.MOV, Register.rax));
            } else {
              b.add(++j, new Instruction(new Memory(Register.rsp, (k - 6) * 8, Type.r64), Op.MOV, call.args.get(k)));
            }
          }
        case 6:
          Op op = call.args.get(5).getType() == Type.r8 ? Op.MOVSX : Op.MOV;
          b.add(++j, new Instruction(Register.r9, op, call.args.get(5)));
        case 5:
          op = call.args.get(4).getType() == Type.r8 ? Op.MOVSX : Op.MOV;
          b.add(++j, new Instruction(Register.r8, op, call.args.get(4)));
        case 4:
          op = call.args.get(3).getType() == Type.r8 ? Op.MOVSX : Op.MOV;
          b.add(++j, new Instruction(Register.rcx, op, call.args.get(3)));
        case 3:
          op = call.args.get(2).getType() == Type.r8 ? Op.MOVSX : Op.MOV;
          b.add(++j, new Instruction(Register.rdx, op, call.args.get(2)));
        case 2:
          op = call.args.get(1).getType() == Type.r8 ? Op.MOVSX : Op.MOV;
          b.add(++j, new Instruction(Register.rsi, op, call.args.get(1)));
        case 1:
          op = call.args.get(0).getType() == Type.r8 ? Op.MOVSX : Op.MOV;
          b.add(++j, new Instruction(Register.rdi, op, call.args.get(0)));
        case 0:
          break;
        }
        if (call.variadic) {
          b.add(++j, new Instruction(Op.MOV, new Imm64(call.variadicXMMArgsCount), Register.rax));
        }
        b.add(++j, new Instruction(Op.CALL, call.a));
        if (ins.dest != Value.dummy) {
          b.add(++j, new Instruction(ins.dest, Op.MOV, Register.rax));
        }
        if (nextInsIsTest) {
          ++j;
        }
        b.add(++j, new Instruction(Value.dummy, Op.END_XCALL));

      } else if (ins.op == Op.GET_ARG) {
        assert (ins.a instanceof Imm64);
        switch ((int) ((Imm64) ins.a).val) {
        case 0:
          b.set(j, new Instruction(ins.dest, Op.MOV, Register.rdi));
          break;
        case 1:
          b.set(j, new Instruction(ins.dest, Op.MOV, Register.rsi));
          break;
        case 2:
          b.set(j, new Instruction(ins.dest, Op.MOV, Register.rdx));
          break;
        case 3:
          b.set(j, new Instruction(ins.dest, Op.MOV, Register.rcx));
          break;
        case 4:
          b.set(j, new Instruction(ins.dest, Op.MOV, Register.r8));
          break;
        case 5:
          b.set(j, new Instruction(ins.dest, Op.MOV, Register.r9));
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

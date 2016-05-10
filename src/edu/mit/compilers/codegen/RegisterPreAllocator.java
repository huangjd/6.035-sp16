package edu.mit.compilers.codegen;

import java.util.HashMap;

import edu.mit.compilers.codegen.Instruction.*;

public class RegisterPreAllocator extends BasicBlockTraverser {

  HashMap<Operand, Register> renameTable;

  @Override
  protected void visit(BasicBlock b) {
    for (int i = 0; i < b.size(); i++) {
      Instruction ins = b.get(i);
      if (ins.op == Op.GET_ARG) {
        switch ((int) ins.a.toLong().longValue()) {
        case 0:
          renameTable.put(ins.dest, Register.rdi);
          b.set(i, new Instruction(Op.DELETED));
          break;
        case 1:
          renameTable.put(ins.dest, Register.rsi);
          b.set(i, new Instruction(Op.DELETED));
          break;
        case 2:
          renameTable.put(ins.dest, Register.rdx);
          b.set(i, new Instruction(Op.DELETED));
          break;
        case 3:
          renameTable.put(ins.dest, Register.rcx);
          b.set(i, new Instruction(Op.DELETED));
          break;
        case 4:
          renameTable.put(ins.dest, Register.r8);
          b.set(i, new Instruction(Op.DELETED));
          break;
        case 5:
          renameTable.put(ins.dest, Register.r9);
          b.set(i, new Instruction(Op.DELETED));
          break;
        }
      }
      if (ins instanceof CallInstruction) {
        CallInstruction call = (CallInstruction) ins;
        for (int j = 0; j < call.args.size(); j++) {
          Register reg = renameTable.get(call.args.get(j));
          if (reg != null) {
            call.args.set(j, reg);
          }
        }
      } else if (ins instanceof DivInstruction) {
        Register reg = renameTable.get(ins.dest2());
        if (reg != null) {
          ((DivInstruction) ins).dest2 = reg;
        }
      }

      Register reg = renameTable.get(ins.dest);
      if (reg != null) {
        ins.dest = reg;
      }
      reg = renameTable.get(ins.a);
      if (reg != null) {
        ins.a = reg;
      }
      reg = renameTable.get(ins.b);
      if (reg != null) {
        ins.b = reg;
      }
    }
  }

  @Override
  public void reset() {
    super.reset();
    renameTable = new HashMap<>();
  }
}

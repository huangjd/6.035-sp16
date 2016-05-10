package edu.mit.compilers.codegen;

import java.util.HashSet;

import edu.mit.compilers.common.Util;

public class LDSE extends BasicBlockTraverser {

  HashSet<Operand> used;
  HashSet<Operand> killed;

  boolean dseable(Op op) {
    switch (op) {
    case ADD:
    case SUB:
    case IMUL:
    case XOR:
    case MOV:
      return true;
    default:
      return false;
    }
  }

  @Override
  protected void visit(BasicBlock b) {
    used = new HashSet<>();
    killed = new HashSet<>();
    for (int i = 0; i < b.size(); i++) {
      Instruction ins = b.get(i);
      if (dseable(ins.op)) {
        for (int j = i + 1; j < b.size() && j < i+ 10; j++) {
          Instruction ins2 = b.get(j);
          if (Util.in(ins2.getReadOperand(), ins.getDestOperand()[0]) || !dseable(ins2.op)) {
            break;
          } else if (Util.in(ins2.getDestOperand(), ins.getDestOperand()[0])){
            b.set(i, new Instruction(Op.DELETED));
            break;
          }
        }
      }
    }
/*
    for (int i = b.size() - 1; i >= 0; i--) {
      Instruction ins = b.get(i);

      if (used.contains(ins.dest)) {
        b.set(i, new Instruction(Op.DELETED));
      } else {
        Operand[] dest = ins.getDestOperand();
        if (dest.length > 0) {
          if (dseable(ins.op)) {
            used.add(dest[0]);
          }
        }
      }
      for (Operand op : ins.getReadOperand()) {
        used.remove(op);
      }
    }
*/
  }

}

package edu.mit.compilers.codegen;

public class TrivialJumpRemove extends BasicBlockTraverser {

  BasicBlock last = null;

  @Override
  protected void visit(BasicBlock b) {
    if (last != null && last.taken == b) {
      if (last.notTaken == null) {
        last.remove(last.size() - 1);
      } else {
        for (int i = last.size() - 1; i >= 0; i--) {
          Instruction ins = last.get(i);
          if (ins.op.jcc()) {
            if (i < last.size() - 1 && last.get(i + 1).op == Op.JMP) {
              Operand a = last.get(i + 1).a;
              switch (ins.op) {
              case JE:
                last.set(i, new Instruction(Op.JNE, a));
                last.remove(i + 1);
                break;
              case JNE:
                last.set(i, new Instruction(Op.JE, a));
                last.remove(i + 1);
                break;
              case JG:
                last.set(i, new Instruction(Op.JLE, a));
                last.remove(i + 1);
                break;
              case JGE:
                last.set(i, new Instruction(Op.JL, a));
                last.remove(i + 1);
                break;
              case JL:
                last.set(i, new Instruction(Op.JGE, a));
                last.remove(i + 1);
                break;
              case JLE:
                last.set(i, new Instruction(Op.JG, a));
                last.remove(i + 1);
                break;
              case JAE:
                last.set(i, new Instruction(Op.JNAE, a));
                last.remove(i + 1);
                break;
              case JNAE:
                last.set(i, new Instruction(Op.JAE, a));
                last.remove(i + 1);
                break;
              }
            }
            break;

          }
        }
      }
    }
    last = b;
  }

}

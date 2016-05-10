package edu.mit.compilers.codegen;

public class JumpOptimize extends BasicBlockTraverser {

  CFG cfg;

  static boolean isEmpty(BasicBlock b) {
    for (Instruction ins : b) {
      if (ins.op != Op.NOP &&
          ins.op != Op.DELETED &&
          ins.op != Op.COMMENT &&
          ins.op != Op.JMP) {
        return false;
      }
    }
    return true;
  }

  @Override
  protected void visit(BasicBlock b) {

    boolean blockRemoved = false;
    if (b.taken != null) {
      while (isEmpty(b.taken)) {
        b.taken.taken.comefroms.remove(b.taken);
        b.taken = b.taken.taken;
        b.taken.comefroms.add(b);
        blockRemoved = true;
      }
    }


    if (b.notTaken != null) {
      while (isEmpty(b.notTaken)) {
        b.notTaken.taken.comefroms.remove(b.notTaken);
        b.notTaken = b.notTaken.taken;
        b.notTaken.comefroms.add(b);
        blockRemoved = true;
      }
    }

    Instruction last = b.get(b.size() - 1);
    if (blockRemoved) {
      b.set(b.size() - 1, new Instruction(last.dest, last.op, b.taken != null ? new JumpTarget(b.taken) : null,
          b.notTaken != null ? new JumpTarget(b.notTaken) : null));
    }

    if (last.op.jcc()) {
      if (b.taken == b.notTaken) {
        b.clearNotTaken();
        b.set(b.size() - 1, new Instruction(Op.JMP, new JumpTarget(b.taken)));
      }
    }

    if (b.taken != null && b.notTaken != null && (b.taken.taken == b.notTaken || b.taken.notTaken == b.notTaken)) {
      for (int i = b.size() - 1; i >= 0; i--) {
        Instruction ins = b.get(i);
        if (ins.op.jcc()) {
          BasicBlock temp = b.taken;
          b.taken = b.notTaken;
          b.notTaken = temp;
          switch (ins.op) {
          case JE:
            b.set(i, new Instruction(Value.dummy, Op.JNE, new JumpTarget(b.taken), new JumpTarget(b.notTaken)));
            break;
          case JNE:
            b.set(i, new Instruction(Value.dummy, Op.JE, new JumpTarget(b.taken), new JumpTarget(b.notTaken)));
            break;
          case JG:
            b.set(i, new Instruction(Value.dummy, Op.JLE, new JumpTarget(b.taken), new JumpTarget(b.notTaken)));
            break;
          case JGE:
            b.set(i, new Instruction(Value.dummy, Op.JL, new JumpTarget(b.taken), new JumpTarget(b.notTaken)));
            break;
          case JL:
            b.set(i, new Instruction(Value.dummy, Op.JGE, new JumpTarget(b.taken), new JumpTarget(b.notTaken)));
            break;
          case JLE:
            b.set(i, new Instruction(Value.dummy, Op.JG, new JumpTarget(b.taken), new JumpTarget(b.notTaken)));
            break;
          case JAE:
            b.set(i, new Instruction(Value.dummy, Op.JNAE, new JumpTarget(b.taken), new JumpTarget(b.notTaken)));
            break;
          case JNAE:
            b.set(i, new Instruction(Value.dummy, Op.JAE, new JumpTarget(b.taken), new JumpTarget(b.notTaken)));
            break;
          }

          break;
        }
      }
    }
  }
}

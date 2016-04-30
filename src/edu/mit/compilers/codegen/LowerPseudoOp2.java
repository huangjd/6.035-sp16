package edu.mit.compilers.codegen;

public class LowerPseudoOp2 extends BasicBlockTraverser {

  static int stage = 15;
  boolean peephole;

  public LowerPseudoOp2(boolean peephole) {
    this.peephole = peephole;
  }

  @Override
  protected void visit(BasicBlock b) {
    for (int i = 0; i < b.size(); i++) {
      Instruction ins = b.get(i);
      assert (ins.twoOperand || ins.op.stage() >= stage);
      switch (ins.op) {
      case OUT_OF_BOUNDS:
      case CONTROL_REACHES_END:
        if (peephole) {
          if (ins.op == Op.OUT_OF_BOUNDS) {
            b.set(i, new Instruction(Op.CALL, new Symbol("_exit.0")));
          } else {
            b.set(i, new Instruction(Op.CALL, new Symbol("_exit.1")));
          }
        } else {
          b.set(i, new Instruction(Op.MOV, ins.dest, Register.rsi));
          b.add(++i, new Instruction(Op.MOV, ins.a, Register.rdx));
          b.add(++i, new Instruction(Op.MOV, ins.b, Register.rcx));
          if (ins.op == Op.OUT_OF_BOUNDS) {
            b.add(++i, new Instruction(Op.CALL, new Symbol("_exit.2")));
          } else {
            b.add(++i, new Instruction(Op.CALL, new Symbol("_exit.3")));
          }
        }
        break;
      default:
        break;
      }
    }
  }
}

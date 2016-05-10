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
      case RANGE:
        assert(ins.b instanceof Imm64);
        long range = ((Imm64)ins.b).val;
        if (ins.a.isImm()) {
          long index = ins.a instanceof Imm8 ? ((Imm8) ins.a).val : ((Imm64) ins.a).val;
          if (index >= 0 && index < range) {
            b.set(i, new Instruction(Op.DELETED));
          } else {
            b.set(i, new Instruction(Op.CALL, new Symbol("_exit.0")));
          }
        } else {
          if (ins.b.isImm64N32()) {
            b.add(i++, new Instruction(Op.MOV, ins.b, Register.rax));
            ins.b = Register.rax;
          }
          b.set(i, new Instruction(Op.CMP, ins.b, ins.a));
          b.add(++i, new Instruction(Op.JAE, new Symbol("_exit.0")));
        }
        break;
      case JMP:
      case RET:
      case NO_RETURN:
        b.set(i, new Instruction(ins.op, ins.a));
        break;
      default:
        if (ins.op.pseudoOp() && !ins.op.isa()) {
          b.set(i, new Instruction(Op.DELETED));
        }
        break;
      }
      if (ins.op.jcc()) {
        b.set(i, new Instruction(ins.op, ins.a));
        b.add(++i, new Instruction(Op.JMP, ins.b));
      }
    }
  }
}

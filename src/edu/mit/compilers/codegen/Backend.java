package edu.mit.compilers.codegen;

import edu.mit.compilers.nodes.*;

public class Backend extends Visitor {

  Register returnValue;
  IRBuilder builder;

  public Backend() {
  }

  protected void compile(ProgramNode node) {
    node.accept(this);
  }

  protected void compile(FunctionNode node) {
    node.accept(this);
  }

  protected void compile(StatementNode node) {
    node.accept(this);
  }

  protected Register compile(ExpressionNode node) {
    returnValue = null;
    node.accept(this);
    assert (returnValue != null);
    Register temp = returnValue;
    returnValue = null;
    return temp;
  }

  @Override
  protected void visit(Add node) {
    Register a = compile(node.left);
    Register b = compile(node.right);
    returnValue = builder.emitOp(Opcode.ADD, a, b);
  }

  @Override
  protected void visit(And node) {
    Register a = compile(node.left);
    BasicBlock evalRight = builder.createBasicBlock();
    BasicBlock exit = builder.createBasicBlock();
    builder.emitBranch(a, evalRight, exit);

    builder.insertBasicBlock(evalRight);
    builder.setCurrentBasicBlock(evalRight);
    Register b = compile(node.right);
    builder.emitBranch(exit);

    builder.insertBasicBlock(exit);
    builder.setCurrentBasicBlock(exit);

  }

  @Override
  protected void visit(Assign node) {
    // TODO implement
  }

  @Override
  protected void visit(Block node) {
    // TODO implement
  }

  @Override
  protected void visit(BooleanLiteral node) {
    returnValue = compile(node.box());
  }

  @Override
  protected void visit(Break node) {
    // TODO implement
  }

  @Override
  protected void visit(Continue node) {
    // TODO implement
  }

  @Override
  protected void visit(Die node) {
    // TODO implement
  }

  @Override
  protected void visit(Div node) {
    Register a = compile(node.left);
    Register b = compile(node.right);
    returnValue = builder.emitDiv(a, b).getKey(); // dividend
  }

  @Override
  protected void visit(Eq node) {
    // TODO implement
  }

  @Override
  protected void visit(For node) {
    // TODO implement
  }

  @Override
  protected void visit(Ge node) {
    Register a = compile(node.left);
    Register b = compile(node.right);
    Register tmp = builder.emitOp(Opcode.SUB, b, a);
    returnValue = builder.emitOp(Opcode.CMP, tmp, new Immediate(1));
  }

  @Override
  protected void visit(Gt node) {
    Register a = compile(node.left);
    Register b = compile(node.right);
    Register tmp = builder.emitOp(Opcode.SUB, b, a);
    returnValue = builder.emitOp(Opcode.CMP, tmp, new Immediate(0));
  }

  @Override
  protected void visit(If node) {
    // TODO implement
  }

  @Override
  protected void visit(IntLiteral node) {
    returnValue = compile(node.box());
  }

  @Override
  protected void visit(Le node) {
    Register a = compile(node.left);
    Register b = compile(node.right);
    Register tmp = builder.emitOp(Opcode.SUB, a, b);
    returnValue = builder.emitOp(Opcode.CMP, tmp, new Immediate(1));
  }

  @Override
  protected void visit(Lt node) {
    Register a = compile(node.left);
    Register b = compile(node.right);
    returnValue = builder.emitOp(Opcode.CMP, a, b);
  }

  @Override
  protected void visit(Length node) {
    Register a = compile(node.box());
    returnValue = Emits.emitLength(a);
  }

  @Override
  protected void visit(Load node) {
    // TODO
  }

  @Override
  protected void visit(Minus node) {
    Register a = compile(node.box());
    returnValue = Emits.emitMinus(a);
  }

  @Override
  protected void visit(Mod node) {
    Register a = compile(node.left);
    Register b = compile(node.right);
    returnValue = builder.emitDiv(a, b).getValue();
  }

  @Override
  protected void visit(Mul node) {
    Register a = compile(node.left);
    Register b = compile(node.right);
    returnValue = builder.emitOp(Opcode.IMUL, a, b);
  }

  @Override
  protected void visit(Ne node) {
    Register a = compile(node.left);
    Register b = compile(node.right);
    Register l = builder.emitOp(Opcode.CMP, a, b);
    Register g = builder.emitOp(Opcode.CMP, b, a);
    returnValue = builder.emitOp(Opcode.NAND, a, b);
  }

  @Override
  protected void visit(Not node) {
    Register b = compile(node.right);
    returnValue = builder.emitOp(Opcode.XOR, b, new Immediate(0)); // flip bits
  }


  @Override
  protected void visit(Or node) {
    // TODO implement short circuiting
  }

  @Override
  protected void visit(Pass node) {
    // TODO pass
  }

  @Override
  protected void visit(Return node) {
    // TODO pass
  }

  @Override
  protected void visit(Store node) {
    // TODO pass
  }

  @Override
  protected void visit(StringLiteral node) {
    returnValue = compile(node.box());
  }

  @Override
  protected void visit(Ternary node) {
    Register cond = compile(node.cond); // TODO check over

    if (cond.value == 1) {
      returnValue = compile(node.trueExpr);
    } else {
      returnValue = compile(node.falseExpr);
    }
  }

  @Override
  protected void visit(UnparsedIntLiteral node) {
    Register a = compile(node.box());
    returnValue = Emits.emitUnparsedIntLiteral(a);
  }

  @Override
  protected void visit(While node) {
    // TODO
  }



  @Override
  protected void visit(Sub node) {
    Register a = compile(node.left);
    Register b = compile(node.right);
    returnValue = Emits.emitSub(a, b);
  }

}

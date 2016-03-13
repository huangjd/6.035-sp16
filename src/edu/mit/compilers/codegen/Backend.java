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

}

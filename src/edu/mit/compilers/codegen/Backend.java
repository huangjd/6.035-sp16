package edu.mit.compilers.codegen;

import edu.mit.compilers.nodes.*;

public class Backend extends Visitor {

  Register returnValue;
  IRBuilder builder;

  public Backend() {
  }

  protected void compile(ProgramNode node) {

  }

  protected void compile(FunctionNode node) {

  }

  protected void compile(StatementNode node) {

  }

  protected Register compile(ExpressionNode node) {

  }

  @Override
  protected void visit(Add node) {
    Register a = compile(node.left);
    Register b = compile(node.right);
    returnValue = builder.emitOp(Opcode.ADD, a, b);
  }

}

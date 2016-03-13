package edu.mit.compilers.nodes;

import edu.mit.compilers.codegen.Register;
import edu.mit.compilers.common.Type;
import edu.mit.compilers.visitors.Codegen;

public class ExpressionNode extends NodeProxy {
  public ExpressionNode(Expression expr) {
    super(expr);
  }

  @Override
  public ExpressionNode accept(Mutator mutator) {
    mutator.returnNode = node;
    node.dispatch(mutator);
    Node temp = mutator.returnNode;
    mutator.returnNode = null;
    return new ExpressionNode((Expression) temp);
  }
  
  public Register accept(Codegen codegen) {
  	// TODO
  	return null;
  }

  public Type getType() {
    return ((Expression) node).getType();
  }
}
package edu.mit.compilers.nodes;

public class StatementNode extends NodeProxy {
  public StatementNode(Statement stmt) {
    super(stmt);
  }

  @Override
  public StatementNode accept(Mutator mutator) {
    mutator.returnNode = node;
    node.dispatch(mutator);
    Node temp = mutator.returnNode;
    mutator.returnNode = null;
    return new StatementNode((Statement) temp);
  }
}
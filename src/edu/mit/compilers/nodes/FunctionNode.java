package edu.mit.compilers.nodes;

public class FunctionNode extends NodeProxy {
  public FunctionNode(Function func) {
    super(func);
  }

  @Override
  public FunctionNode accept(Mutator mutator) {
    mutator.returnNode = node;
    node.dispatch(mutator);
    Node temp = mutator.returnNode;
    mutator.returnNode = null;
    return new FunctionNode((Function) temp);
  }
}
package edu.mit.compilers.nodes;

public class ProgramNode extends NodeProxy {
  public ProgramNode(Program prog) {
    super(prog);
  }

  @Override
  public ProgramNode accept(Mutator mutator) {
    mutator.returnNode = node;
    node.dispatch(mutator);
    Node temp = mutator.returnNode;
    mutator.returnNode = null;
    return new ProgramNode((Program) temp);
  }
}
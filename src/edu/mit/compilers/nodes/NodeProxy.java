package edu.mit.compilers.nodes;

import edu.mit.compilers.common.SourcePosition;

public abstract class NodeProxy {
  Node node;

  protected NodeProxy(Node node) {
    this.node = node;
  }

  public void accept(Visitor visitor) {
    node.dispatch(visitor);
  }

  public abstract NodeProxy accept(Mutator mutator);

  public SourcePosition getSourcePosition() {
    return node.getSourcePosition();
  }

  @Override
  public String toString() {
    return node.toString();
  }

  @Override
  public int hashCode() {
    return node.hashCode();
  }

  public Node getNode() {
    return node;
  }
}

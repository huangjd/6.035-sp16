package edu.mit.compilers.nodes;

public abstract class NodeProxy {
  Node node;

  protected NodeProxy(Node node) {
    this.node = node;
  }

  public void accept(Visitor visitor) {
    node.dispatch(visitor);
  }

  public abstract NodeProxy accept(Mutator mutator);

  @Override
  public String toString() {
    return node.toString();
  }

  @Override
  public int hashCode() {
    return node.hashCode();
  }
}

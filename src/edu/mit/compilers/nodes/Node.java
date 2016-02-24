package edu.mit.compilers.nodes;

import edu.mit.compilers.common.SourcePosition;

public abstract class Node {
  protected SourcePosition position;
  protected int hashCache;

  abstract void dispatch(Visitor visitor);

  protected Node(SourcePosition pos) {
    position = pos;
  }

  public SourcePosition getSourcePosition() {
    return position;
  }

  @Override
  public String toString() {
    return "";
  }

  @Override
  public int hashCode() {
    return hashCache;
  }
}

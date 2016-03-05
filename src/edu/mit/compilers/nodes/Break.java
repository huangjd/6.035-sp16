package edu.mit.compilers.nodes;

import edu.mit.compilers.common.SourcePosition;

public class Break extends Statement {

  public Breakable context;

  public Break(Breakable node, SourcePosition pos) {
    super(pos);
    this.context = node;
    if (node != null) {
      hashCache = node.hashCode();
    }
  }

  @Override
  void dispatch(Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  public String toString() {
    return "break;\n";
  }
}

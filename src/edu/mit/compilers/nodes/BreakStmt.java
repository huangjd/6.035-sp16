package edu.mit.compilers.nodes;

import edu.mit.compilers.common.SourcePosition;

public class BreakStmt extends Statement {

  public Breakable context;

  public BreakStmt(Breakable node, SourcePosition pos) {
    super(pos);
    this.context = node;
    hashCache = node.hashCode();
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

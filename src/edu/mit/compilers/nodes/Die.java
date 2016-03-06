package edu.mit.compilers.nodes;

import edu.mit.compilers.common.SourcePosition;

public class Die extends Statement {

  public final int exitCode;

  public Die(int exitCode, SourcePosition pos) {
    super(pos);
    this.exitCode = exitCode;
    this.hashCache = exitCode;
  }

  @Override
  void dispatch(Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  public String toString() {
    return "// exit(" + String.valueOf(exitCode) + ");\n";
  }
}

package edu.mit.compilers.nodes;

import edu.mit.compilers.common.SourcePosition;

public class Die extends Statement {

  public static int EXIT_SUCCESS = 0;
  public static int ARRAY_INDEX_OUT_OF_BOUNDS = -1;
  public static int CONTROL_REACHES_END_OF_NON_VOID_FUNCTION = -2;

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

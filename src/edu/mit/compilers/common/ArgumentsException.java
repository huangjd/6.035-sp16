package edu.mit.compilers.common;

import edu.mit.compilers.nodes.Function;

public class ArgumentsException extends RuntimeException {

  private static final long serialVersionUID = 5856910631990125011L;

  public final Function func;
  public final int expected, actual;
  public final SourcePosition pos;

  public ArgumentsException(Function func, int actual, SourcePosition pos) {
    this.func = func;
    this.expected = func.getParams().size();
    this.actual = actual;
    this.pos = pos;
    if (expected == actual) {
      throw new IllegalArgumentException("Internal Error in throwing an ArgumentsException: expected == actual");
    }
  }
}

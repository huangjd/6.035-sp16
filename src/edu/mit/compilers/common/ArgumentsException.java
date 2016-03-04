package edu.mit.compilers.common;

public class ArgumentsException extends RuntimeException {

  private static final long serialVersionUID = 5856910631990125011L;

  public final int expected, actual;
  public final SourcePosition pos;

  public ArgumentsException(int expected, int actual, SourcePosition pos) {
    this.expected = expected;
    this.actual = actual;
    this.pos = pos;
    if (expected == actual) {
      throw new IllegalArgumentException("Internal Error in throwing an ArgumentsException: expected == actual");
    }
  }
}

package edu.mit.compilers.common;

public class ArgumentsException extends RuntimeException {

  private static final long serialVersionUID = 5856910631990125011L;

  public final int expected, actual;

  public ArgumentsException(int expected, int actual) {
    this.expected = expected;
    this.actual = actual;
  }
}

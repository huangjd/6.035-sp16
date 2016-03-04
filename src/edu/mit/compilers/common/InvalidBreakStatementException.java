package edu.mit.compilers.common;

public class InvalidBreakStatementException extends RuntimeException {

  private static final long serialVersionUID = 1706805744994860565L;

  public final SourcePosition pos;
  public final boolean isContinue; // 0 for break, 1 for continue

  public InvalidBreakStatementException(boolean isContinue, SourcePosition pos) {
    this.pos = pos;
    this.isContinue = isContinue;
  }
}

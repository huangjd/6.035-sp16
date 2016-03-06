package edu.mit.compilers.common;

public class GeneralException extends RuntimeException {

  private static final long serialVersionUID = 6633493662522198864L;

  public final SourcePosition pos;
  public final String msg;

  public GeneralException(String msg, SourcePosition pos) {
    this.msg = msg;
    this.pos = pos;
  }
}

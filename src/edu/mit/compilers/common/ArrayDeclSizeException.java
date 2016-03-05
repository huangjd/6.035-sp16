package edu.mit.compilers.common;

public class ArrayDeclSizeException extends RuntimeException {

  private static final long serialVersionUID = 5489295201813410061L;
  public final String value;
  public final SourcePosition pos;

  public ArrayDeclSizeException(String id, SourcePosition pos) {
    this.value = id;
    this.pos = pos;
  }
}

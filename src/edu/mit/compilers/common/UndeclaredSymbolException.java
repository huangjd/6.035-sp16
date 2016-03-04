package edu.mit.compilers.common;

public class UndeclaredSymbolException extends RuntimeException {

  private static final long serialVersionUID = -8504457388312127862L;

  public final String symbol;
  public final SymbolTable symbolTable;
  public final MethodTable methodTable;
  public final SourcePosition pos;

  public UndeclaredSymbolException(String symbol, SymbolTable symbolTable, SourcePosition pos) {
    this.symbol = symbol;
    this.symbolTable = symbolTable;
    this.methodTable = null;
    this.pos = pos;
  }

  public UndeclaredSymbolException(String symbol, MethodTable methodTable, SourcePosition pos) {
    this.symbol = symbol;
    this.symbolTable = null;
    this.methodTable = methodTable;
    this.pos = pos;
  }
}

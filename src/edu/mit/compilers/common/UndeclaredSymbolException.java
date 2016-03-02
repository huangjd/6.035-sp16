package edu.mit.compilers.common;

public class UndeclaredSymbolException extends RuntimeException {

  private static final long serialVersionUID = -8504457388312127862L;

  public final String symbol;

  public UndeclaredSymbolException(String symbol) {
    this.symbol = symbol;
  }
}

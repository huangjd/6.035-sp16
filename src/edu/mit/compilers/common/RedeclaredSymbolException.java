package edu.mit.compilers.common;

public class RedeclaredSymbolException extends RuntimeException {

  private static final long serialVersionUID = 527004853582608985L;

  public final Var original, redeclared;

  public RedeclaredSymbolException(Var original, Var redeclared) {
    this.original = original;
    this.redeclared = redeclared;
  }
}

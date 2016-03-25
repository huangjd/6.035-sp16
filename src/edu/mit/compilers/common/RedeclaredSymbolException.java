package edu.mit.compilers.common;

import edu.mit.compilers.nodes.Function;

public class RedeclaredSymbolException extends RuntimeException {

  private static final long serialVersionUID = 527004853582608985L;

  public final Var original, redeclared;
  public final SourcePosition pos;
  public final Function f;

  public RedeclaredSymbolException(Var original, Var redeclared, SourcePosition pos) {
    this.original = original;
    this.redeclared = redeclared;
    this.pos = pos;
    this.f = null;
  }

  public RedeclaredSymbolException(Function f, Var var, SourcePosition pos) {
    this.original = var;
    this.redeclared = null;
    this.pos = pos;
    this.f = f;
  }

  public RedeclaredSymbolException(Function f, SourcePosition pos) {
    this.original = null;
    this.redeclared = null;
    this.pos = pos;
    this.f = f;
  }
}

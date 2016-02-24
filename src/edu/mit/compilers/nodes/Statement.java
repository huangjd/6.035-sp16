package edu.mit.compilers.nodes;

import edu.mit.compilers.common.SourcePosition;

abstract class Statement extends Node {
  protected Statement(SourcePosition pos) {
    super(pos);
  }

  public StatementNode box() {
    return new StatementNode(this);
  }
}


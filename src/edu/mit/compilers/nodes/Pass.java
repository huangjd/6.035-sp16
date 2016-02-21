package edu.mit.compilers.nodes;

import edu.mit.compilers.common.SourcePosition;

class Pass extends Statement {

  private int nop;

  protected Pass(SourcePosition pos) {
    super(pos);
    nop = 0;
  }

  protected Pass(int nNop, SourcePosition pos) {
    super(pos);
    nop = nNop;
  }

  @Override
  public int hashCode() {
    return nop;
  };

  @Override
  public String toString() {
    return "pass";
  };

  public int getValue() {
    return nop;
  }

  @Override
  void dispatch(Visitor visitor) {
    visitor.visit(this);
  }
}

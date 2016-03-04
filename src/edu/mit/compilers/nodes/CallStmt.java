package edu.mit.compilers.nodes;

import edu.mit.compilers.common.SourcePosition;

public class CallStmt extends Statement {

  public final Call call;

  public CallStmt(Call call, SourcePosition pos) {
    super(pos);
    this.call = call;
    this.hashCache = call.hashCode();
  }

  @Override
  void dispatch(Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  public String toString() {
    return call.toString() + ";\n";
  }
}

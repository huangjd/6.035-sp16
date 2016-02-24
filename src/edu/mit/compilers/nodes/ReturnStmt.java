package edu.mit.compilers.nodes;

import edu.mit.compilers.common.SourcePosition;

public class ReturnStmt extends Statement {

  public Function context;
  public ExpressionNode value;

  public ReturnStmt(Function node, SourcePosition pos) {
    super(pos);
    this.context = node;
    this.value = null;
    hashCache = node.hashCode();
  }

  public ReturnStmt(Function node, ExpressionNode value, SourcePosition pos) {
    super(pos);
    this.context = node;
    this.value = value;
    hashCache = node.hashCode() + 43 * value.hashCode();
  }

  @Override
  void dispatch(Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  public String toString() {
    return "return" + (value != null ? " " + value.toString() : "") + ";\n";
  }
}

package edu.mit.compilers.nodes;

import java.util.ArrayList;

import edu.mit.compilers.common.*;

public class Call extends Expression {

  public Function func;
  public ArrayList<ExpressionNode> args;

  public Call(Function func, ArrayList<ExpressionNode> args, SourcePosition pos) {
    super(pos);
    this.func = func;
    this.args = args;
    hashCache = func.getName().hashCode() * 991 + args.hashCode();
  }

  @Override
  void dispatch(Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  public String toString() {
    StringBuilder s = new StringBuilder();
    for (ExpressionNode expr : args) {
      s.append(expr.toString());
      s.append(", ");
    }
    String str = s.toString();
    str = str.substring(0, str.length() - 2);

    return func.getName() + "(" + str + ")";
  }

  @Override
  public Type getType() {
    return func.returnType;
  }
}

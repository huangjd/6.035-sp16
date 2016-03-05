package edu.mit.compilers.nodes;

import java.util.ArrayList;

import edu.mit.compilers.common.*;

public class Call extends Expression {

  public final Function func;
  public final ArrayList<ExpressionNode> args;
  public final String tempName;

  public Call(Function func, ArrayList<ExpressionNode> args, SourcePosition pos) {
    super(pos);
    this.func = func;
    this.args = args;
    this.tempName = null;

    ArrayList<Var> parameters = func.parameters.asList();

    if (!func.isCallout) {
      if (parameters.size() != args.size()) {
        throw new ArgumentsException(func, args.size(), pos);
      }

      for (int i = 0; i < parameters.size(); i++) {
        if (parameters.get(i).type != args.get(i).getType()) {
          throw new TypeException(args.get(i), parameters.get(i).type);
        }
      }
    }

    hashCache = func.getName().hashCode() * 991 + args.hashCode();
  }

  public Call(String tempName, ArrayList<ExpressionNode> args, SourcePosition pos) {
    super(pos);
    this.func = null;
    this.args = args;
    this.tempName = tempName;

    hashCache = args.hashCode();
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

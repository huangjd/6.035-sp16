package edu.mit.compilers.common;

import edu.mit.compilers.nodes.ExpressionNode;

public class TypeException extends RuntimeException {

  private static final long serialVersionUID = 1993536259564632481L;

  public enum SubType {
    MISMATCH,
    EXPECTED_PRIMITIVE,
    EXPECTED_ARRAY,
    EXPECTED_VARIABLE,
    EXPECTED_CALL,
    CONFLICT
  }

  public final SubType subtype;
  public final Type expected, actual;
  public final ExpressionNode expr, expr2;
  public final Var var;

  private TypeException(SubType subType, Type expected, Type actual, ExpressionNode left, ExpressionNode right,
      Var var) {
    this.subtype = subType;
    this.expected = expected;
    this.actual = actual;
    this.expr = left;
    this.expr2 = right;
    this.var = var;
  }

  public TypeException(Var var) {
    this(SubType.EXPECTED_VARIABLE, null, var.type, null, null, var);
  }

  public TypeException(ExpressionNode expr) {
    this(SubType.EXPECTED_VARIABLE, null, expr.getType(), expr, null, null);
  }

  public TypeException(Var var, boolean isArray) {
    this(isArray ? SubType.EXPECTED_ARRAY : SubType.EXPECTED_PRIMITIVE,
        null, var.type, null, null, var);
  }

  public TypeException(ExpressionNode expr, boolean isArray) {
    this(isArray ? SubType.EXPECTED_ARRAY : SubType.EXPECTED_PRIMITIVE,
        null, expr.getType(), expr, null, null);
  }

  public TypeException(Var var, ExpressionNode value) {
    this(SubType.CONFLICT, var.type, value.getType(), value, null, var);
  }

  public TypeException(Var var, Type expected) {
    this(SubType.MISMATCH, expected, var.type, null, null, var);
  }

  public TypeException(ExpressionNode expr, Type expected) {
    this(SubType.MISMATCH, expected, expr.getType(), expr, null, null);
  }

  public TypeException(SubType subType, Type actual) {
    this(subType, null, actual, null, null, null);
  }

  public TypeException(Type expected, Type actual) {
    this(SubType.MISMATCH, expected, actual, null, null, null);
  }

  public TypeException(ExpressionNode left, ExpressionNode right) {
    this(SubType.CONFLICT, null, null, left, right, null);
  }
}

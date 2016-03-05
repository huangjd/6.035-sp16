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
    CONFLICT,
    NONE,
  }

  public final SubType subtype;
  public final Type expected, actual;
  public final ExpressionNode expr, expr2;
  public final Var var;
  public final SourcePosition pos;

  private TypeException(SubType subType, Type expected, Type actual, ExpressionNode left, ExpressionNode right,
      Var var, SourcePosition pos) {
    this.subtype = subType;
    this.expected = expected;
    this.actual = actual;
    this.expr = left;
    this.expr2 = right;
    this.var = var;
    this.pos = pos;
  }

  public TypeException(TypeException type, SourcePosition pos) {
    this(type.subtype, type.expected, type.actual, type.expr, type.expr2, type.var, pos);
  }

  public TypeException(Var var, SourcePosition pos) {
    this(SubType.EXPECTED_VARIABLE, null, var.type, null, null, var, pos);
  }

  public TypeException(ExpressionNode expr) {
    this(SubType.EXPECTED_VARIABLE, null, expr.getType(), expr, null, null, expr.getSourcePosition());
  }

  public TypeException(Var var, boolean isArray, SourcePosition pos) {
    this(isArray ? SubType.EXPECTED_ARRAY : SubType.EXPECTED_PRIMITIVE,
        null, var.type, null, null, var, pos);
  }

  public TypeException(ExpressionNode expr, boolean isArray) {
    this(isArray ? SubType.EXPECTED_ARRAY : SubType.EXPECTED_PRIMITIVE,
        null, expr.getType(), expr, null, null, expr.getSourcePosition());
  }

  public TypeException(Var var, ExpressionNode value) {
    this(SubType.CONFLICT, var.type, value.getType(), value, null, var, value.getSourcePosition());
  }

  public TypeException(Var var, Type expected, SourcePosition pos) {
    this(SubType.MISMATCH, expected, var.type, null, null, var, pos);
  }

  public TypeException(ExpressionNode expr, Type expected) {
    this(SubType.MISMATCH, expected, expr.getType(), expr, null, null, expr.getSourcePosition());
  }

  public TypeException(Type expected, Type actual, SourcePosition pos) {
    this(SubType.MISMATCH, expected, actual, null, null, null, pos);
  }

  public TypeException(Type actual, boolean isArray, SourcePosition pos) {
    this(isArray ? SubType.EXPECTED_ARRAY : SubType.EXPECTED_PRIMITIVE, null, actual, null, null, null, pos);
  }

  public TypeException(ExpressionNode left, ExpressionNode right) {
    this(SubType.CONFLICT, left.getType(), right.getType(), left, right, null, right.getSourcePosition());
  }
}

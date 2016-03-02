package edu.mit.compilers.common;

public enum Type {
  NONE,
  BOOLEAN,
  INT,
  BOOLEANARRAY,
  INTARRAY,
  STRING,
  CALL,
  CALLOUT;

  @Override
  public String toString() {
    switch (this) {
    case NONE:
      return "void";
    case BOOLEAN:
      return "boolean";
    case INT:
      return "int";
    case BOOLEANARRAY:
      return "boolean[]";
    case INTARRAY:
      return "int[]";
    case STRING:
      return "string";
    case CALL:
      return "function";
    case CALLOUT:
      return "callout";
    default:
      return null;
    }
  }

  public boolean isArray() {
    return this == Type.BOOLEANARRAY || this == Type.INTARRAY;
  }

  public boolean isPrimitive() {
    return this == Type.BOOLEAN || this == Type.INT;
  }

  public boolean isVariable() {
    return isPrimitive() || isArray();
  }

  public Type getElementType() {
    if (this == BOOLEANARRAY) {
      return BOOLEAN;
    } else if (this == INTARRAY) {
      return INT;
    } else {
      throw new TypeException(TypeException.SubType.EXPECTED_ARRAY, this);
    }
  }
}

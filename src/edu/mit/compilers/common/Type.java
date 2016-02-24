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
      return "<boolean[]>";
    case INTARRAY:
      return "<int[]>";
    case STRING:
      return "<string>";
    case CALL:
      return "<func>";
    case CALLOUT:
      return "<extern func>";
    default:
      return null;
    }
  }
}

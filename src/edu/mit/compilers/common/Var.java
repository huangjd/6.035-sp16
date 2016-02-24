package edu.mit.compilers.common;

public class Var {
  public final String id;
  public final Type type;
  public final int size;
  public final int length;

  int stackOffset;
  int registerIndex = -1;

  public Var(String id, Type type) {
    this.id = id;
    this.type = type;
    this.length = 0;
    switch (type) {
    case INT:
      size = 8;
      break;
    case BOOLEAN:
      size = 1;
      break;
    default:
      size = 0;
      break;
    }
  }

  public Var(String id, Type type, int length) {
    this.id = id;
    this.type = type;
    this.length = length;
    switch (type) {
    case INTARRAY:
      size = 8 * length;
      break;
    case BOOLEAN:
      size = length;
      break;
    default:
      size = 0;
      break;
    }
  }

  public boolean isArray() {
    return type == Type.BOOLEANARRAY || type == Type.INTARRAY;
  }

  public boolean isPrimitive() {
    return type == Type.BOOLEAN || type == Type.INT;
  }

  public boolean isVariable() {
    return isPrimitive() || isArray();
  }

  @Override
  public String toString() {
    return id;
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}

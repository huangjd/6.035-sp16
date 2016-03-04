package edu.mit.compilers.common;

public class Var {
  public final String id;
  public final Type type;
  public final long size;
  public final long length;

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

  public Var(String id, Type type, long length) {
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
    return type.isArray();
  }

  public boolean isPrimitive() {
    return type.isPrimitive();
  }

  public boolean isVariable() {
    return type.isVariable();
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

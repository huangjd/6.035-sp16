package edu.mit.compilers.common;

public class Var {
  public final String id;
  public final Type type;
  public final long size;
  public final long length;

  public int stackOffset = 0;
  public int registerIndex = -1;
  public boolean bss = false;

  public Var(String id, Type type) {
    this.id = id;
    this.type = type;
    this.length = 0;
    switch (type) {
    case INT:
      size = 8;
      break;
    case BOOLEAN:
      size = 8;
      break;
    default:
      throw new TypeException(type, false, null);
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
    case BOOLEANARRAY:
      size = 8 * length;
      break;
    default:
      throw new TypeException(type, true, null);
    }
  }

  public Var(String id, String str) {
    this.id = id;
    this.type = Type.STRING;
    this.size = str.length() + 1;
    this.length = str.length() + 1;
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

  public int getStackOffset() {
    return stackOffset;
  }
}

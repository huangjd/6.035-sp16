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
      size = 1;
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
      size = length;
      break;
    default:
      throw new TypeException(type, true, null);
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

  public int getStackOffset() {
    return stackOffset;
  }
}

package edu.mit.compilers.common;

public class Var {

  String name;
  Type type;

  public Var(String name, Type type) {
    this.name = name;
    switch (type) {
    case BOOLEAN:
    case INT:
      this.type = type;
      break;
    default:
      // TODO: error;
      break;
    }
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public String toString() {
    return name;
  }

  public Type getType() {
    return type;
  }
}

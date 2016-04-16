package edu.mit.compilers.codegen;

public class Value extends Operand {

  static int counter = 130;
  public static final Value dummy = new Value(128, Type.r64);
  public static final Value dummyb = new Value(129, Type.r8);

  public final int id;
  public final Type type;

  public Value() {
    id = counter;
    type = Type.r64;
    counter++;
  }

  public Value(boolean dummy) {
    id = counter;
    type = Type.r8;
    counter++;
  }

  public Value(Type type) {
    id = counter;
    this.type = type;
    counter++;
  }

  Value(int i, Type type) {
    id = i;
    this.type = type;
  }

  @Override
  public String toString() {
    if (id != dummy.id && id != dummyb.id) {
      return "r" + String.valueOf(id);
    } else {
      return "_";
    }
  }

  @Override
  public Type getType() {
    return type;
  }

  @Override
  public boolean isPointer() {
    return false;
  }
}

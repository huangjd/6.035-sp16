package edu.mit.compilers.codegen;

public abstract class Operand {
  public enum Type {
    r8, r64, xmm, ymm;

    @Override
    public String toString() {
      switch (this) {
      case r8:
        return "[r8]";
      case r64:
        return "[r64]";
      case xmm:
        return "[xmm]";
      case ymm:
        return "[ymm]";
      }
      return "";
    }
  }

  abstract public Type getType();

  abstract public boolean isPointer();

  @Override
  abstract public String toString();
}

class JumpTarget extends Operand {
  BasicBlock target;

  public JumpTarget(BasicBlock target) {
    this.target = target;
  }

  @Override
  public Type getType() {
    return Type.r64;
  }

  @Override
  public boolean isPointer() {
    return false;
  }

  @Override
  public String toString() {
    return target.label;
  }
}

class Imm64 extends Operand {
  public final long val;

  public Imm64(long val) {
    this.val = val;
  }

  @Override
  public String toString() {
    return "$" + String.valueOf(val);
  }

  @Override
  public Type getType() {
    return Type.r64;
  }

  @Override
  public boolean isPointer() {
    return false;
  }
}

class Imm8 extends Operand {
  public final byte val;

  public Imm8(byte val) {
    this.val = val;
  }

  public Imm8(boolean val) {
    this.val = val ? (byte) 1 : (byte) 0;
  }

  @Override
  public String toString() {
    return "$" + String.valueOf(val);
  }

  @Override
  public Type getType() {
    return Type.r8;
  }

  @Override
  public boolean isPointer() {
    return false;
  }
}

class Symbol extends Operand {
  String symbol;

  public Symbol(String s) {
    symbol = s;
  }

  @Override
  public Type getType() {
    return Type.r64;
  }

  @Override
  public String toString() {
    return symbol;
  }

  @Override
  public boolean isPointer() {
    return false;
  }
}

class Array extends Operand {
  Type type;
  long size;
  int id;

  static int counter;

  public Array(Type type, long size) {
    this.type = type;
    this.size = size;
    this.id = counter;
    counter++;
  }

  @Override
  public Type getType() {
    return type;
  }

  @Override
  public String toString() {
    return "A" + String.valueOf(id);
  }

  @Override
  public boolean isPointer() {
    return true;
  }
}

class BSSObject extends Operand {
  String symbol;
  Type type;
  boolean isArray;
  long length;

  public BSSObject(String symbol, Type type) {
    if (type != Type.r8 && type != Type.r64) {
      throw new RuntimeException();
    }
    this.symbol = symbol;
    this.type = type;
    this.isArray = false;
    this.length = 1;
  }

  public BSSObject(String symbol, Type type, long length) {
    if (type != Type.r8 && type != Type.r64) {
      throw new RuntimeException();
    }
    this.symbol = symbol;
    this.type = type;
    this.isArray = true;
    this.length = length;
  }

  @Override
  public Type getType() {
    return type;
  }

  @Override
  public String toString() {
    if (isArray) {
      return "$" + symbol;
    } else {
      return symbol;
    }
  }

  @Override
  public boolean isPointer() {
    return isArray;
  }
}

class StringObject extends Operand {
  String symbol;
  String content;
  static int counter;

  public StringObject(String s) { // for strings
    symbol = ".LC" + String.valueOf(counter);
    content = s;
    counter++;
  }

  @Override
  public Type getType() {
    return Type.r64;
  }

  @Override
  public boolean isPointer() {
    return true;
  }

  @Override
  public String toString() {
    return "$" + symbol;
  }
}

class Memory extends Operand {
  Register base;
  Register index;
  int offset;
  Type multiplier;

  public Memory(Register base, Register index, int offset, Type multiplier) {
    this.base = base;
    this.index = index;
    this.offset = offset;
    this.multiplier = multiplier;
    assert (multiplier == Type.r8 || multiplier == Type.r64);
  }

  public Memory(Register base, int offset, Type multiplier) {
    this.base = base;
    this.index = null;
    this.offset = offset;
    this.multiplier = multiplier;
    assert (multiplier == Type.r8 || multiplier == Type.r64);
  }

  @Override
  public Type getType() {
    return multiplier;
  }

  @Override
  public String toString() {
    String mult;
    switch (multiplier) {
    case r8:
      mult = "";
      break;
    case r64:
      mult = ", 8";
      break;
    default:
      throw new RuntimeException();
    }
    return (offset != 0 ? String.valueOf(offset) : "") + "(" + base.toString()
    + (index != null ? ", " + index.toString() : "") + mult + ")";
  }

  @Override
  public boolean isPointer() {
    return true;
  }
}

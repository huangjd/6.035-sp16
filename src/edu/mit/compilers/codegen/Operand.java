package edu.mit.compilers.codegen;

import edu.mit.compilers.common.Util;

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

  boolean isReg() {
    return this instanceof Register;
  }

  boolean isMem() {
    return this instanceof Memory || this instanceof BSSObject || this instanceof StringObject;
  }

  boolean isImm() {
    return this instanceof Imm8 || this instanceof Imm64;
  }

  boolean isImm32() {
    return this instanceof Imm8 || this instanceof Imm64 && Util.isImm32(((Imm64) this).val);
  }

  boolean isImm64N32() {
    return this instanceof Imm64 && !Util.isImm32(((Imm64) this).val);
  }

  Register[] getInvolvedRegs() {
    return new Register[]{};
  }

  @Override
  public abstract boolean equals(Object arg0);

  public String toString(Type type) {
    return toString();
  }
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

  @Override
  public boolean equals(Object arg0) {
    return arg0 instanceof JumpTarget && ((JumpTarget) arg0).target == target;
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

  @Override
  public boolean equals(Object arg0) {
    return arg0 instanceof Imm64 && ((Imm64) arg0).val == val;
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

  @Override
  public boolean equals(Object arg0) {
    return arg0 instanceof Imm8 && ((Imm8) arg0).val == val;
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

  @Override
  public boolean equals(Object arg0) {
    return arg0 instanceof Symbol && ((Symbol) arg0).symbol.equals(symbol);
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

  @Override
  public boolean equals(Object arg0) {
    if (!(arg0 instanceof Array)) {
      return false;
    }
    Array a0 = (Array) arg0;
    return a0.type.equals(type) && a0.size == size && a0.id == id;
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
    return symbol;
  }

  @Override
  public boolean isPointer() {
    return isArray;
  }

  @Override
  public boolean equals(Object arg0) {
    return arg0 instanceof BSSObject && ((BSSObject) arg0).symbol.equals(symbol);
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

  @Override
  public boolean equals(Object arg0) {
    return arg0 instanceof StringObject && ((StringObject) arg0).symbol.equals(symbol);
  }
}

class Memory extends Operand {
  Register base;
  Register index;
  int offset;
  BSSObject bssoffset;
  Type multiplier;

  public Memory(Register base, Register index, int offset, Type multiplier) {
    this.base = base;
    this.index = index;
    this.offset = offset;
    this.bssoffset = null;
    this.multiplier = multiplier;
    assert (multiplier == Type.r8 || multiplier == Type.r64);
    assert (base != null);
  }

  public Memory(Register base, int offset, Type multiplier) {
    this.base = base;
    this.index = null;
    this.offset = offset;
    this.bssoffset = null;
    this.multiplier = multiplier;
    assert (multiplier == Type.r8 || multiplier == Type.r64);
    assert (base != null);
  }

  public Memory(BSSObject base, Register index, Type multiplier) {
    this.base = null;
    this.index = index;
    this.offset = 0;
    this.bssoffset = base;
    this.multiplier = multiplier;
    assert (multiplier == Type.r8 || multiplier == Type.r64);
  }

  public Memory(BSSObject base, int offset, Type multiplier) {
    this.base = null;
    this.index = null;
    this.offset = offset;
    this.bssoffset = base;
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
      mult = "1";
      break;
    case r64:
      mult = "8";
      break;
    default:
      throw new RuntimeException();
    }

    if (bssoffset!= null) {
      if (index != null) {
        return bssoffset.toString() + "(," + index.toString() + "," + mult + ")";
      } else {
        return bssoffset.toString() + "+" + String.valueOf(offset) + "*" + mult;
      }
    } else {
      return (offset != 0 ? String.valueOf(offset) : "") + "(" + base.toString()
      + (index != null ? "," + index.toString() + "," + mult : "") + ")";
    }
  }

  @Override
  public boolean isPointer() {
    return true;
  }

  @Override
  Register[] getInvolvedRegs() {
    if (base != null) {
      if (index != null) {
        return new Register[]{base, index};
      } else {
        return new Register[]{base};
      }
    } else {
      if (index != null) {
        return new Register[]{index};
      } else {
        return new Register[]{};
      }
    }
  }

  @Override
  public boolean equals(Object arg0) {
    if (!(arg0 instanceof Memory)) {
      return false;
    }
    Memory a0 = (Memory) arg0;
    return a0.base.equals(base) && (a0.index == null && index == null || a0.index.equals(index))
        && a0.offset == offset && a0.multiplier.equals(multiplier);
  }
}

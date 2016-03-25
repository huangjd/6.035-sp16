package edu.mit.compilers.codegen;

import edu.mit.compilers.codegen.Value.OperandType;

public class Value {
  public enum OperandType {
    r8, r16, r32, r64, xmm, ymm;

    @Override
    public String toString() {
      switch (this) {
      case r8:
        return "int8 ";
      case r16:
        return "int16";
      case r32:
        return "int32";
      case r64:
        return "int64";
      case xmm:
        return "xmm";
      case ymm:
        return "ymm";
      default:
        throw new IllegalStateException();
      }
    };
  }

  protected OperandType getType() {
    return value.type;
  }

  public ValueImpl value;

  public Value(ValueImpl val) {
    this.value = val;
  }
}

abstract class ValueImpl {
  public OperandType type;

  Value box() {
    return new Value(this);
  }
}

class Immediate extends ValueImpl {
  long value;

  public Immediate(long value) {
    this.value = value;
    this.type = OperandType.r64;
  }

  @Override
  public String toString() {
    return "$" + Long.toString(value);
  }
}

class Memory extends ValueImpl {
  Value base, index, offset;
  int sizeofelement;

  public Memory(Value base, Value index, Value offset, int sizeofelement) {
    assert (offset == null || offset.value instanceof Immediate || offset.value instanceof Symbol);
    this.base = base;
    this.index = index;
    this.sizeofelement = sizeofelement;
    this.offset = offset;

    switch (sizeofelement) {
    case 1:
      this.type = OperandType.r8;
      break;
    case 2:
      this.type = OperandType.r16;
      break;
    case 4:
      this.type = OperandType.r32;
      break;
    case 8:
      this.type = OperandType.r64;
      break;
    default:
      throw new RuntimeException("Invalid multiplier for imm(reg, reg, multiplier)");
    }
  }

  public Memory(Value base, Value index, long offset, int sizeofelement) {
    this(base, index, new Immediate(offset).box(), sizeofelement);
  }

  public Memory(Value base, Value index, Value offset, int sizeofelement, OperandType type) {
    this(base, index, offset, sizeofelement);

    this.type = type;
    switch (sizeofelement) {
    case 1:
    case 2:
    case 4:
    case 8:
      break;
    default:
      throw new RuntimeException("Invalid multiplier for imm(reg, reg, multiplier)");
    }
  }

  public Memory(Value base, Value index, long offset, int sizeofelement, OperandType type) {
    this(base, index, new Immediate(offset).box(), sizeofelement, type);
  }

  @Override
  public String toString() {
    return (offset != null ? offset.toString() : "") + "(" + base.toString() +
        (index != null ? ", " + index.toString() : "") +
        (sizeofelement == 1 ? "" : ", " + Integer.toString(sizeofelement)) + ")";
  }
}

class Symbol extends ValueImpl {
  String symbol;

  public Symbol(String s) {
    symbol = s;
    this.type = OperandType.r64;
  }

  @Override
  public String toString() {
    return symbol;
  }
}

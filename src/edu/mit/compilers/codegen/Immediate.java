package edu.mit.compilers.codegen;

import edu.mit.compilers.codegen.Value.OperandType;

public class Immediate extends ValueImpl {
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
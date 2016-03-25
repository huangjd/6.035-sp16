package edu.mit.compilers.codegen;

public class Instruction {

  public Opcode op;
  public Value a, b, c;

  public Instruction dependency;
  public int dependencyMode;
  public static int FOLLOW_IMMEDIATELY = 1;
  public static int NO_RFLAGS_MODIFICATION = 2;

  // ISA form instruction, need explicit check
  public Instruction(Opcode op) {
    this(op, null, null, null);
  }

  public Instruction(Opcode op, Value a) {
    this(op, a, null, null);
  }

  public Instruction(Opcode op, Value a, Value b) {
    this(op, a, b, null);
  }

  public Instruction(Opcode op, Value a, Value b, Value c) {
    this.op = op;
    this.a = a;
    this.b = b;
    this.c = c;
  }

  public Instruction addDependency(Instruction previous, int mode) {
    dependency = previous;
    dependencyMode = mode;
    return this;
  }

  @Override
  public String toString() {
    return op.toString() +
        (a != null ? " " + a.toString() : "") +
        (b != null ? " ," + b.toString() : "") +
        (c != null ? " ," + c.toString() : "");
  }
}

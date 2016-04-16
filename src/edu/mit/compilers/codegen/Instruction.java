package edu.mit.compilers.codegen;

import java.util.ArrayList;

import edu.mit.compilers.common.Util;

public class Instruction {
  public boolean twoOperand;
  public Op op;
  public Operand a, b;
  public Operand dest;

  static public class DivInstruction extends Instruction {
    public boolean mod;

    public DivInstruction(Operand dest, Operand a, Operand b, boolean mod) {
      super(dest, Op.FAKE_DIV, a, b);
      this.mod = mod;
    }

    @Override
    public String toString() {
      return super.toString() + "\t(" + (mod ? "mod" : "div") + ")";
    }
  }

  static public class CallInstruction extends Instruction {
    public ArrayList<Operand> args;
    public boolean variadic;
    public int variadicXMMArgsCount;

    public CallInstruction(Operand dest, Operand symbol, ArrayList<Operand> args, boolean variadic,
        int variadicXMMArgsCount) {
      super(dest, Op.FAKE_CALL, symbol);
      this.args = args;
      this.variadic = variadic;
      this.variadicXMMArgsCount = variadicXMMArgsCount;
    }

    @Override
    public String toString() {
      return super.toString() + "\t(" + Util.toCommaSeparatedString(args) + ")"
          + (variadic ? "\tvariadic with " + String.valueOf(variadicXMMArgsCount) + " XMM args" : "");
    }
  }

  public Instruction(Operand dest, Op op, Operand a, Operand b) {
    this.twoOperand = false;
    this.a = a;
    this.b = b;
    this.op = op;
    this.dest = dest;
  }

  public Instruction(Operand dest, Op op, Operand a) {
    this(dest, op, a, null);
  }

  public Instruction(Operand dest, Op op) {
    this(dest, op, null, null);
  }

  public Instruction(Op op, Operand a, Operand b) {
    this(null, op, a, b);
    twoOperand = true;
  }

  public Instruction(Op op, Operand a) {
    this(null, op, a, null);
    twoOperand = true;
  }

  public Instruction(Op op) {
    this(null, op, null);
    twoOperand = true;
  }

  @Override
  public String toString() {
    if (twoOperand) {
      return op.toString() + '\t' + (a != null? a.toString() : "")
          + (b != null ? ", " + b.toString() : "");
    } else {
      return (dest != null ? dest.toString() + " = " : "\t") +
          op.toString() + '\t' + (a != null? a.toString() : "")
          + (b != null ? ", " + b.toString() : "");
    }
  }
}
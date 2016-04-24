package edu.mit.compilers.codegen;

import java.util.ArrayList;

import edu.mit.compilers.common.Util;

public class Instruction {
  public boolean twoOperand;
  public Op op;
  public Operand a, b;
  public Operand dest;

  static public class DivInstruction extends Instruction {
    public Operand dest2;

    public DivInstruction(Operand dest1, Operand dest2, Operand a, Operand b) {
      super(dest1, Op.FAKE_DIV, a, b);
      assert (dest2 != null);
      this.dest2 = dest2;
    }

    @Override
    public String toString() {
      return dest.toString() + ", " + dest2.toString() + " = x_div\t" + a.toString() + ",\t" + b.toString();
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
      for (Operand op : args) {
        assert (op != null);
      }
    }

    @Override
    public String toString() {
      return super.toString() + "\t(" + Util.toCommaSeparatedString(args) + ")"
          + (variadic ? "\tvariadic with " + String.valueOf(variadicXMMArgsCount) + " XMM args" : "");
    }
  }

  static public class HardCode extends Instruction {
    String content;

    public HardCode(String s) {
      super(Op.NOP);
      content = s;
    }

    @Override
    public String toString() {
      return content;
    };
  }

  public Instruction(Operand dest, Op op, Operand a, Operand b) {
    assert (op.pseudoOp());
    assert (dest != null);
    if (!op.special()) {
      if (op.pseudoOpDestMustBeDummy()) {
        assert (dest == Value.dummy);
      } else {
        assert (dest != Value.dummy);
      }
    }
    assert (a != Value.dummy && b != Value.dummy);
    int operandNum = op.pseudoOpOperandNun();
    switch (operandNum) {
    case 0:
      assert (a == null && b == null);
      break;
    case 1:
      assert (a != null && b == null);
      break;
    case 2:
      assert (a != null && b != null);
      break;
    case 3:
    default:
      throw new RuntimeException();
    }

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
    assert (op.isa());
    assert (a != Value.dummy && b != Value.dummy);
    int operandNum = op.isaOerandNum();
    switch (operandNum) {
    case 0:
      assert (a == null && b == null);
      break;
    case 1:
      assert (a != null && b == null);
      break;
    case 2:
      assert (a != null && b != null);
      break;
    case 3:
    default:
      throw new RuntimeException();
    }
    assert (!op.hasSuffix() || operandNum > 0);

    this.a = a;
    this.b = b;
    this.op = op;
    this.dest = null;
    twoOperand = true;
  }

  public Instruction(Op op, Operand a) {
    this(op, a, null);
  }

  public Instruction(Op op) {
    this(op, null, null);
  }

  public Operand[] getDest() {
    ArrayList<Operand> dests = new ArrayList<>();
    int dest = op.isaWriteDest();
    if (dest == 1) {
      dests.add(a);
    }
    if (dest == 2) {
      dests.add(b);
    }
    return (Operand[]) dests.toArray();
  }

  @Override
  public String toString() {
    if (twoOperand) {
      Operand.Type opType = Operand.Type.r64;
      if (b != null) {
        opType = b.getType();
      } else if (a != null) {
        opType = a.getType();
      }
      return op.toString(opType) + '\t' + (a != null ? a.toString() : "")
          + (b != null ? ", " + b.toString() : "");
    } else {
      Operand.Type opType = dest.getType();
      return dest.toString() + " =\t" + op.toString(opType) +
          '\t' + (a != null ? a.toString() : "") +
          (b != null ? ", " + b.toString() : "");
    }
  }
}
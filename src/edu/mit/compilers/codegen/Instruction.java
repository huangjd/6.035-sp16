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
    assert (!(dest instanceof Imm8 || dest instanceof Imm64 || dest instanceof StringObject || dest instanceof Symbol
        || dest instanceof Array)
        || (dest instanceof StringObject && (op == Op.CONTROL_REACHES_END || op == Op.OUT_OF_BOUNDS))
        || (dest instanceof Array && op == Op.LOCAL_ARRAY_DECL)
        || ((op == Op.LOAD || op == Op.STORE) && (a instanceof Array || a instanceof BSSObject)
            && !(b instanceof Array)));
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
    int operandNum = op.isaOperandNum();
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

    switch (op) {
    case IDIV:
      assert (a.isReg() || a.isMem());
      break;
    case MOV:
      if (a.isMem()) {
        assert (b.isReg());
      }
      if (b.isMem()) {
        assert (a.isReg() || a.isImm32());
      }
      break;
    case CALL:
      break;
    default:
      if (op.isaOperandNum() == 2) {
        assert (!a.isImm64N32() && !b.isImm() && !(a.isMem() && b.isMem()));
      } else if (op.isaOperandNum() == 1) {
        if (op.ctrlx() == 1 || op.ctrlx() == 2) {
          assert (a instanceof JumpTarget);
        } else {
          assert (a.isMem() || a.isReg());
        }
      }
    }

    int destWrite = op.isaWriteDest();
    switch (destWrite) {
    case 0:
      break;
    case 1:
      assert (a.isReg() || a.isMem() || a instanceof Value);
      break;
    case 2:
      assert (b.isReg() || b.isMem() || b instanceof Value);
      break;
    }

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

  public Register[] getRegRead() {
    if ((op == Op.SUB || op == Op.XOR) && a instanceof Register && a.equals(b) && a != Register.rxx) {
      return new Register[]{};
    }
    ArrayList<Register> regs = new ArrayList<Register>();
    int readsrc = op.isaReadSrc();
    if ((readsrc & 1) != 0 || a != null && a.isMem()) {
      for (Register reg : a.getInvolvedRegs()) {
        regs.add(reg);
      }
    }
    if ((readsrc & 2) != 0 || b != null && b.isMem()) {
      for (Register reg : b.getInvolvedRegs()) {
        regs.add(reg);
      }
    }

    return regs.toArray(new Register[]{});
  }

  public Register[] getRegWrite() {
    switch (op.isaWriteDest()) {
    case 0:
      return new Register[]{};
    case 1:
      if (a.isReg()) {
        return a.getInvolvedRegs();
      } else {
        return new Register[]{};
      }
    case 2:
      if (b.isReg()) {
        return b.getInvolvedRegs();
      } else {
        return new Register[]{};
      }
    case 3:
    default:
      throw new RuntimeException();
    }
  }

  @Override
  public String toString() {
    if (twoOperand) {
      Operand.Type opType = Operand.Type.r64;
      if (b != null) {
        opType = b.getType();
        if (opType == Operand.Type.r8 && a.getType() == Operand.Type.r64) {
          opType = Operand.Type.r64;
        }
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
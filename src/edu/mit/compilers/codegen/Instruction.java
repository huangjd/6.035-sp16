package edu.mit.compilers.codegen;

import java.util.*;

import edu.mit.compilers.common.Util;
import edu.mit.compilers.common.Util.Predicate;

public class Instruction {
  public boolean twoOperand;
  public Op op;
  public Operand a, b;
  public Operand dest;

  public Instruction(Instruction ins) {
    this.op = ins.op;
    this.a = ins.a;
    this.b = ins.b;
    this.dest = ins.dest;
    this.twoOperand = ins.twoOperand;
  }

  @Override
  protected Instruction clone() {
    return new Instruction(this);
  }

  static Predicate<Operand> dummyFilter = new Predicate<Operand>() {
    @Override
    public boolean eval(Operand obj) {
      return (obj != Value.dummy);
    }
  };

  /*
   * static public class HardCode extends Instruction {
   * String content;
   *
   * public HardCode(String s) {
   * super(Op.NOP);
   * content = s;
   * }
   *
   * @Override
   * public String toString() {
   * return content;
   * };
   * }
   */

  public Instruction(Operand dest, Op op, Operand a, Operand b) {
    /*//if (op.special())
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
    }*/

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
    /*if (op != Op.RANGE && !op.isAnnotation()) {
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
            assert (a instanceof JumpTarget || a instanceof Symbol);
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
    }*/

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
    if ((op == Op.SUB || op == Op.XOR) && a instanceof Register && a.equals(b)) {
      return new Register[]{};
    }
    HashSet<Register> regs = new HashSet<Register>();
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

  public void rename(AbstractMap<? extends Operand, ? extends Operand> renameTable) {
    if (dest != Value.dummy) {
      Operand n = renameTable.get(dest);
      if (n != null) {
        dest = n;
      }
    }
    if (a != null) {
      Operand n = renameTable.get(a);
      if (n != null) {
        a = n;
      }
    }
    if (b != null) {
      Operand n = renameTable.get(b);
      if (n != null) {
        b = n;
      }
    }
  }

  public Operand dest2() {
    return null;
  }

  public Operand[] getDestOperand() {
    if (twoOperand) {
      switch (op.isaWriteDest()) {
      case 0:
        return new Operand[]{};
      case 1:
        return new Operand[]{a};
      case 2:
        return new Operand[]{b};
      default:
        throw new RuntimeException();
      }
    } else {
      if (op.isAnnotation()) {
        return new Operand[]{};
      }
      switch (op) {
      case STORE:
        return new Operand[]{};
      default:
        return Util.filter(new Operand[]{dest}, dummyFilter);
      }
    }
  }

  public Operand[] getReadOperand() {
    if (twoOperand) {
      switch (op.isaReadSrc()) {
      case 0:
        return new Operand[]{};
      case 1:
        return new Operand[]{a};
      case 2:
        return new Operand[]{b};
      case 3:
        return new Operand[]{a, b};
      default:
        throw new RuntimeException();
      }
    } else {
      if (op.isAnnotation()) {
        return new Operand[]{};
      }
      switch (op) {
      case STORE:
        return new Operand[]{a, b, dest};
      default:
        return Util.filter(new Operand[]{a, b}, dummyFilter);
      }
    }
  }

  @Override
  public String toString() {
    if (op == Op.HACK_IMUL) {
      return op.toString(b.getType()) + "\t" + dest.toString() + ",\t" + a.toString() + ",\t" + b.toString(b.getType());
    }

    if (op == Op.COMMENT) {
      return "/* " + ((StringObject) a).content + " */";
    }

    if (twoOperand) {
      if (op == Op.MOVSX) {
        if (a.isImm()) {
          return new Instruction(Op.MOV, a, b).toString();
        } else {
          return op.toString() + '\t' + a.toString(Operand.Type.r8) + ",\t" + b.toString(Operand.Type.r64);
        }
      }
      if (op.setcc()) {
        return op.toString() + '\t' + a.toString(Operand.Type.r8);
      }

      if (op.hasSuffix()) {
        Operand.Type opType;
        if (b != null) {
          opType = b.getType();
        } else if (a != null) {
          opType = a.getType();
        } else {
          opType = Operand.Type.r64;
        }

        return op.toString(opType) + '\t' + (a != null ? a.toString(opType) : "")
            + (b != null ? ", " + b.toString(opType) : "");
      } else {
        return op.toString() + '\t' + (a != null ? a.toString() : "")
            + (b != null ? ", " + b.toString() : "");
      }
    } else {
      Operand.Type opType = dest.getType();
      return dest.toString(opType) + " =\t" + op.toString(opType) +
          '\t' + (a != null ? a.toString(opType) : "") +
          (b != null ? ", " + b.toString(opType) : "");
    }
  }

  static public class DivInstruction extends Instruction {
    public Operand dest2;

    public DivInstruction(DivInstruction ins) {
      super(ins);
      this.dest2 = ins.dest2;
    }

    @Override
    protected DivInstruction clone() {
      return new DivInstruction(this);
    }

    public DivInstruction(Operand dest1, Operand dest2, Operand a, Operand b) {
      super(dest1, Op.FAKE_DIV, a, b);
      assert (dest2 != null);
      this.dest2 = dest2;
    }

    @Override
    public String toString() {
      return dest.toString() + ", " + dest2.toString() + " = x_div\t" + a.toString() + ",\t" + b.toString();
    }

    @Override
    public Operand[] getDestOperand() {
      return Util.filter(new Operand[]{dest, dest2}, dummyFilter);
    }

    @Override
    public Operand dest2() {
      return dest2;
    }
  }

  static public class CallInstruction extends Instruction {
    public ArrayList<Operand> args;
    public boolean variadic;
    public int variadicXMMArgsCount;

    public CallInstruction(CallInstruction ins) {
      super(ins);
      this.args = (ArrayList<Operand>) ins.args.clone();
      this.variadic = ins.variadic;
      this.variadicXMMArgsCount = ins.variadicXMMArgsCount;
    }

    @Override
    protected Instruction clone() {
      return new CallInstruction(this);
    }

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

    @Override
    public Operand[] getReadOperand() {
      return args.toArray(new Operand[]{});
    }

    @Override
    public Register[] getRegRead() {
      HashSet<Register> regs = new HashSet<>();
      for (Operand arg : args) {
        for (Register reg : arg.getInvolvedRegs()) {
          regs.add(reg);
        }
      }
      return regs.toArray(new Register[]{});
    }
  }

  static ArrayList<Instruction> emitMov(Operand src, Operand dest) {
    ArrayList<Instruction> arr = new ArrayList<>();
    if (dest.isReg() || dest.isMem() && (src.isReg() || src.isImm32())) {
      arr.add(new Instruction(Op.MOV, src, dest));
    } else {
      arr.add(new Instruction(Op.MOV, src, Register.rax));
      arr.add(new Instruction(Op.MOV, Register.rax, dest));
    }
    return arr;
  }
}
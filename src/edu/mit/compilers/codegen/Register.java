package edu.mit.compilers.codegen;

public class Register extends Value {

  public int id;
  public OperandType type;

  public Register(int id) {
    this.id = id;
    this.type = OperandType.r64;
  }

  public Register(int id, OperandType type) {
    this.id = id;
    this.type = type;
  }

  @Override
  public String toString() {
    if (id > 64) {
      return "#r" + Integer.toString(id) + " [" + type.toString() + "]";
    } else {
      if (id >= 0 && id < 16) {
        switch (type) {
        case r8:
          return RegisterNames[0][id];
        case r16:
          return RegisterNames[1][id];
        case r32:
          return RegisterNames[2][id];
        case r64:
          return RegisterNames[3][id];
        default:
          throw new RuntimeException("Invalid logical register index/type combination");
        }
      } else if (id >= 16 && id < 32) {
        switch (type) {
        case xmm:
          return "%xmm" + Integer.toString(id - 16);
        case ymm:
          return "%ymm" + Integer.toString(id - 16);
        default:
          throw new RuntimeException("Invalid logical register index/type combination");
        }
      } else {
        switch (id) {
        case -1:
          switch (type) {
          case r32:
            return "%eip";
          case r64:
            return "%rip";
          default:
            throw new RuntimeException("Invalid logical register index/type combination");
          }
        case -2:
          switch (type) {
          case r32:
            return "%eflags";
          case r64:
            return "%rflags";
          default:
            throw new RuntimeException("Invalid logical register index/type combination");
          }
        default:
          throw new RuntimeException("Invalid logical register index");
        }
      }
    }
  }

  public static final String[][] RegisterNames = {{
    "%al", "%cl", "%dl", "%bl",
    "%spl", "%bpl", "%sil", "%dil",
    "%r8b", "%r9b", "%r10b", "%r11b",
    "%r12b", "%r13b", "%r14b", "%r15b"
  }, {
    "%ax", "%cx", "%dx", "%bx",
    "%sp", "%bp", "%si", "%di",
    " %r8w", " %r9w", "%r10w", "%r11w",
    "%r12w", "%r13w", "%r14w", "%r15w"
  }, {
    "%eax", "%ecx", "%edx", "%ebx",
    "%esp", "%ebp", "%esi", "%edi",
    " %r8d", " %r9d", "%r10d", "%r11d",
    "%r12d", "%r13d", "%r14d", "%r15d"
  }, {
    "%rax", "%rcx", "%rdx", "%rbx",
    "%rsp", "%rbp", "%rsi", "%rdi",
    " %r8", " %r9", "%r10", "%r11",
    "%r12", "%r13", "%r14", "%r15"
  }
  };

  public static final int rax = 0;
  public static final int rcx = 1;
  public static final int rdx = 2;
  public static final int rbx = 3;
  public static final int rsp = 4;
  public static final int rbp = 5;
  public static final int rsi = 6;
  public static final int rdi = 7;

  public static final int r8 = 8;
  public static final int r9 = 9;
  public static final int r10 = 10;
  public static final int r11 = 11;
  public static final int r12 = 12;
  public static final int r13 = 13;
  public static final int r14 = 14;
  public static final int r15 = 15;

  public static Register RSP = new Register(rsp);
  public static Register RBP = new Register(rbp);
  // public static Register RFLAGS = null;

  public static int argToReg(int n) {
    final int[] index = {rdi, rsi, rdx, rcx, r8, r9};
    if (n < 0 || n > 5) {
      throw new IllegalArgumentException();
    }
    return index[n];
  }

  public static int regToArg(int n) {
    final int[] index = {-1, 3, 2, -1, -1, -1, 1, 0,
        4, 5, -1, -1, -1, -1, -1, -1};
    if (n < 0 || n > 15) {
      throw new IllegalArgumentException();
    }
    int res = index[n];
    if (res == -1) {
      throw new IllegalArgumentException();
    }
    return res;
  }
}



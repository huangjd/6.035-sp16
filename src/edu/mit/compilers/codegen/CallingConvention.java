package edu.mit.compilers.codegen;

public interface CallingConvention {
  public int getNthArgRegIndex (int n);

  public int getRetReg();

  public int getNumArgsPassedByReg();

  public int getCalleeNthArgOffsetRbp(int n);

  public int getCallerNthArgOffsetRsp(int n);

  public int[] getCallerSavedRegs();

  public int[] getCalleeSavedRegs();
}

class CallingConventionX86_64Linux implements CallingConvention {
  @Override
  public int getNthArgRegIndex(int n) {
    switch (n) {
    case 0:
      return Register.rdi;
    case 1:
      return Register.rsi;
    case 2:
      return Register.rdx;
    case 3:
      return Register.rcx;
    case 4:
      return Register.r8;
    case 5:
      return Register.r9;
    default:
      throw new IllegalArgumentException();
    }
  }

  @Override
  public int getRetReg() {
    return Register.rax;
  }

  @Override
  public int getNumArgsPassedByReg() {
    return 6;
  }

  @Override
  public int getCalleeNthArgOffsetRbp(int n) {
    if (n < 6) {
      throw new IllegalArgumentException();
    }
    return 8 * (n - 4);
  }

  @Override
  public int getCallerNthArgOffsetRsp(int n) {
    if (n < 6) {
      throw new IllegalArgumentException();
    }
    return 8 * (n - 6);
  }

  @Override
  public int[] getCallerSavedRegs() {
    return new int[]{Register.rax, Register.rcx, Register.rdx, Register.rsi, Register.rdi,
        Register.r8, Register.r9, Register.r10, Register.r11};
  }

  @Override
  public int[] getCalleeSavedRegs() {
    return new int[]{Register.rbx, Register.r12, Register.r13, Register.r14, Register.r15};
  }
}

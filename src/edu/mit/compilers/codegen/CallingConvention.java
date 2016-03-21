package edu.mit.compilers.codegen;

public class CallingConvention {
  public int getNthArg(int n) {
  }

  public int getRetReg() {
    return Register.rax;
  }

  public int getNumArgsPassedByReg() {
    return 6;
  }
}

class CallingConventionX86_64Linux extends CallingConvention {
}

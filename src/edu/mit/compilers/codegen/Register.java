package edu.mit.compilers.codegen;

public class Register {
  public static final int rax = 0;
  public static final int rbx = 1;
  public static final int rcx = 2;
  public static final int rdx = 3;
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
  
  public int value;
  
  public Register(int value) {
  	this.value = value;
  }
  
  public static Register RSP = null; // TODO initialize
  public static Register RBP = null;
  public static ComparisonTypes lastComparisonResult = ComparisonTypes.NONE;

  public static int argToReg(int n) {
    final int[] index = {7, 6, 3, 2, 8, 9};
    if (n < 0 || n > 5) {
      throw new IllegalArgumentException();
    }
    return index[n];
  }

  public static int regToArg(int n) {
    final int[] index = {-1, -1, 3, 2, -1, -1, 1, 0,
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

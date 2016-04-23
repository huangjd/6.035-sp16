package edu.mit.compilers.codegen;

public enum Op {

  ADD("add",    1,  0,  1,  1,  2),
  SUB("sub",    1,  0,  1,  1,  2),
  IMUL("imul",  1,  0,  0,  1,  2),
  IDIV("idiv",  1,  0,  0,  1,  0),
  NOT("not",    1,  0,  1,  1,  1),
  NEG("neg",    1,  0,  1,  1,  1),
  XOR("xor",    1,  0,  1,  1,  2),
  INC("inc",    1,  0,  1,  1,  1),
  DEC("dec",    1,  0,  1,  1,  1),

  MOV("mov",    1,  0,  0,  0,  2),
  PUSH("push",  1,  0,  0,  0,  0),
  POP("pop",    1,  0,  0,  0,  1),

  CMP("cmp",    1,  0,  1,  1,  0),
  TEST("test",  1,  0,  1,  1,  0),

  SETE("sete",  0,  0,  0,  0,  1),
  SETNE("setne",0,  0,  0,  0,  1),
  SETG("setg",  0,  0,  0,  0,  1),
  SETGE("setge",0,  0,  0,  0,  1),
  SETL("setl",  0,  0,  0,  0,  1),
  SETLE("setle",0,  0,  0,  0,  1),

  JE("je",      0,  1,  0,  0,  0),
  JNE("jne",    0,  1,  0,  0,  0),
  JG("jg",      0,  1,  0,  0,  0),
  JGE("jge",    0,  1,  0,  0,  0),
  JL("jl",      0,  1,  0,  0,  0),
  JLE("jle",    0,  1,  0,  0,  0),

  JMP("jmp",    0,  2,  0,  0,  0),
  CALL("call",  0,  0,  0,  1,  0),
  RET("ret",    0,  4,  0,  1,  0),

  NOP("nop",    0,  0,  0,  0,  0),

  // Pseudo ops from midend, need to be lowered in backend
  LOCAL_ARRAY_DECL("local_array", 0), 
  FAKE_DIV("xdiv",1,0,  0,  1,  0), // d = a / b (div|mod)
  LOAD("load", 1), // d = a[b]
  STORE("store", 1), // a[b] = d
  FAKE_CALL("xcall",0,0, 0, 1,  0), // d = call $a (args...) #xmm used
  OUT_OF_BOUNDS("out_of_bounds", 0), // d = funcName a = line, b = col
  CONTROL_REACHES_END("control_reaches_end", 0), // d = funcName a = line, b = col
  ALLOCATE("allocate", 0), // a = Imm64 (set up for func call)
  //SAVE_REG("save_reg", 0),
  //SAVE_REG_DIV("save_reg_div", 0),
  //RESTORE_REG("restore_reg", 0),
  BEGIN_XCALL("begin_xcall", 0),
  BEGIN_XDIV("begin_xdiv", 0),
  END_XCALL("end_xcall", 0),
  END_XDIV("end_xdiv", 0),
  PROLOGUE("func_prologue", 0),
  GET_ARG("get_arg", 1), // d = var, a = Imm64 (index of arg)
  EPILOGUE("func_epilogue", 0),
  NO_RETURN("#no_ret",0,0,0,0,  0),
  DELETED("", 0)
  ;

  String mnemonics;
  int hasSuffix;
  int ctrlx = 0;
  int clearflag = 0;
  int setflag = 0;
  int hasDest = 0;

  public static int CTRLXjcc = 1;
  public static int CTRLXjmp = 2;
  public static int CTRLXret = 4;

  Op(String s, int hasPrefix) {
    mnemonics = s;
    this.hasSuffix = hasPrefix;
  }

  Op(String s, int hasPrefix, int ctrlx, int setflag, int clearflag, int hasDest) {
    mnemonics = s;
    this.hasSuffix = hasPrefix;
    this.ctrlx = ctrlx;
    this.setflag = setflag;
    this.clearflag = clearflag;
    this.hasDest = hasDest;
  }

  @Override
  public String toString() {
    return mnemonics;
  }

  public String toString(Value.Type type) {
    String suffix = "";
    switch (type) {
    case r8:
      suffix = "b";
      break;
    case r64:
      suffix = "q";
      break;
    }

    if (hasSuffix == 1) {
      return mnemonics + suffix;
    } else {
      return mnemonics;
    }
  }
}

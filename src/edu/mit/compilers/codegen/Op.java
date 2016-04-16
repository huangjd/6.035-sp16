package edu.mit.compilers.codegen;

public enum Op {
  ADD("add", 1),
  SUB("sub", 1),
  IMUL("imul", 1),
  IDIV("idiv", 1),
  NOT("not", 1),
  NEG("neg", 1),
  XOR("xor", 1),
  INC("inc", 1),
  DEC("dec", 1),

  MOV("mov", 1),
  PUSH("push", 1),
  POP("pop", 1),

  CMP("cmp", 1),
  TEST("test", 1),

  SETE("sete", 0),
  SETNE("setne", 0),
  SETG("setg", 0),
  SETGE("setge", 0),
  SETL("setl", 0),
  SETLE("setle", 0),

  JE("je", 0),
  JNE("jne", 0),
  JG("jg", 0),
  JGE("jge", 0),
  JL("jl", 0),
  JLE("jle", 0),

  JMP("jmp", 0),
  CALL("call", 0),
  RET("ret", 0),

  NOP("nop", 0),

  // Pseudo ops from midend, need to be lowered in backend
  FAKE_DIV("xdiv", 1), // d = a / b (div|mod)
  LOAD("load", 1), // d = a[b]
  STORE("store", 1), // a[b] = d
  FAKE_CALL("xcall", 0), // d = call $a (args...) #xmm used
  OUT_OF_BOUNDS("out_of_bounds", 0), // d = funcName a = line, b = col
  CONTROL_REACHES_END("control_reaches_end", 0), // d = funcName a = line, b = col
  ALLOCATE("allocate", 0), // a = Imm64 (set up for func call)
  PROLOGUE("func_prologue", 0),
  GET_ARG("get_arg", 1), // d = var, a = Imm64 (index of arg)
  EPILOGUE("func_epilogue", 0),
  DELETED("#--deleted--", 0)
  ;

  String mnemonics;
  int hasSuffix;

  Op(String s, int hasPrefix) {
    mnemonics = s;
    this.hasSuffix = hasPrefix;
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

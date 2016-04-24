package edu.mit.compilers.codegen;

import edu.mit.compilers.common.Util;

public enum Op {

  /* Bit | usage
   * 0    Can be used as ISA Op
   * 1    Can be used as Pseudo Op
   * 2 3  ISA operand count (0-3)
   * 4-5  set if operand 1, 2, 3 is written to, or 0 none
   * 7    set if additional reg is written to (call, idiv)
   * 8    Non ISA dest is dummy? (either must be dummy or mustn't be)
   * 9 10 Non ISA operand count (0-3)
   * 11
   * 12   has suffix
   * 13   has XMM form
   *
   * 16 17 control flow: 0 = no jump, 1 = jcc; 2 = jmp; 3 = ret/noret
   * 18   invalidate flag
   * 19   set flag (implies 18)
   * 20   test flag
   * 24   setcc
   * 25   cmovcc
   * 26   jcc
   * 28-31 Pseudo op may not appear after this stage in fixed pipeline
   *                 C   8   4   0   C   8   4   0
   */
  ADD("add",    0b01110000000011000011010000101011),
  SUB("sub",    0b01110000000011000011010000101011),
  IMUL("imul",  0b01110000000001000011010000101011),
  IDIV("idiv",  0b00000000000001000001000010000101),
  NOT("not",    0b01110000000011000001001000010111),
  NEG("neg",    0b01110000000011000001001000010111),
  XOR("xor",    0b01110000000011000011010000101011),
  INC("inc",    0b01110000000011000001001000010111),
  DEC("dec",    0b01110000000011000001001000010111),
  SAL("sal",    0b01110000000011000011010000101011),
  SAR("sar",    0b01110000000011000011010000101011),

  MOV("mov",    0b01110000000000000011001000101011),
  LEA("lea",    0b01110000000000000001001000101011),
  // PUSH("push", 1, 0, 0, 0, 0),
  // POP("pop", 1, 0, 0, 0, 1),

  CMP("cmp",    0b01110000000011000001010100001011),
  TEST("test",  0b01110000000011000001010100001011),

  SETE("sete",  0b01110001000100000000000000010111),
  SETNE("setne",0b01110001000100000000000000010111),
  SETG("setg",  0b01110001000100000000000000010111),
  SETGE("setge",0b01110001000100000000000000010111),
  SETL("setl",  0b01110001000100000000000000010111),
  SETLE("setle",0b01110001000100000000000000010111),

  CMOVE("cmove",0b00000010000100000001000000101001),
  CMOVNE("cmovne",0b00000010000100000001000000101001),
  CMOVG("cmovg",0b00000010000100000001000000101001),
  CMOVGE("cmovge",0b00000010000100000001000000101001),
  CMOVL("cmovl",0b00000010000100000001000000101001),
  CMOVLE("cmovle",0b00000010000100000001000000101001),

  JE("je",      0b00000100000100010000010100000111),
  JNE("jne",    0b00000100000100010000010100000111),
  JG("jg",      0b00000100000100010000010100000111),
  JGE("jge",    0b00000100000100010000010100000111),
  JL("jl",      0b00000100000100010000010100000111),
  JLE("jle",    0b00000100000100010000010100000111),

  JMP("jmp",    0b00000000000000100000001100000111),
  CALL("call",  0b00000000000001000000000010000101),
  RET("ret",    0b00000000000000110000000100000001),

  NOP("nop",    0b11110000000000000000000100000011),

  // Pseudo ops from midend, need to be lowered in backend
  LOCAL_ARRAY_DECL("local_array",
      0b11010000000000000001001000000010),
  FAKE_DIV("xdiv",0b00000000000001000001010010000010), // d = a / b (div|mod)
  LOAD("load",  0b01110000000000000001010000000010), // d = a[b]
  STORE("store",0b01110000000000000001010000000010), // a[b] = d
  FAKE_CALL("xcall",
      0b00000000000001000001001010000010), // d = call $a (args...) #xmm used
  OUT_OF_BOUNDS("out_of_bounds",
      0b11110000000000000000010000000010), // d = funcName a = line, b = col
  CONTROL_REACHES_END("control_reaches_end",
      0b11110000000000000000010000000010), // d = funcName a = line, b = col
  ALLOCATE("allocate",
      0b11010000000000000000001100000010), // a = Imm64 (set up for func call)
  BEGIN_XCALL("begin_xcall",
      0b10100000000000000000000100000010),
  BEGIN_XDIV("begin_xdiv",
      0b10100000000000000000000100000010),
  END_XCALL("end_xcall",
      0b10100000000000000000000100000010),
  END_XDIV("end_xdiv",
      0b10100000000000000000000100000010),
  PROLOGUE("func_prologue",
      0b11010000000000000000000100000010),
  GET_ARG("get_arg",
      0b00000000000000000001001000000010), // d = var, a = Imm64 (index of arg)
  EPILOGUE("func_epilogue",
      0b11010000000000000000000100000010),
  NO_RETURN("#no_ret",
      0b11110000000000110000000100000011),
  DELETED("",   0b11110000000000000000000100000011)
  ;

  String mnemonics;
  int property;

  Op(String s, int property) {
    mnemonics = s;
    this.property = property;
  }

  public boolean isa() {
    return Util.extractNBit(property, 0, 1) != 0;
  }

  public boolean pseudoOp() {
    return Util.extractNBit(property, 1, 1) != 0;
  }

  public int isaOerandNum() {
    return Util.extractNBit(property, 2, 2);
  }

  public int isaWriteDest() {
    return Util.extractNBit(property, 4, 3);
  }

  public boolean special() {
    return Util.extractNBit(property, 7, 1) != 0;
  }

  public boolean pseudoOpDestMustBeDummy() {
    return Util.extractNBit(property, 8, 1) != 0;
  }

  public int pseudoOpOperandNun() {
    return Util.extractNBit(property, 9, 2);
  }

  public boolean hasSuffix() {
    return Util.extractNBit(property, 12, 1) != 0;
  }

  public boolean hasSIMDForm() {
    return Util.extractNBit(property, 13, 1) != 0;
  }

  public int ctrlx() {
    return Util.extractNBit(property, 16, 2);
  }

  public boolean invalidateFlag() {
    return Util.extractNBit(property, 18, 1) != 0;
  }

  public boolean setFlag() {
    return Util.extractNBit(property, 19, 1) != 0;
  }

  public boolean testFlag() {
    return Util.extractNBit(property, 20, 1) != 0;
  }

  public boolean setcc() {
    return Util.extractNBit(property, 24, 1) != 0;
  }

  public boolean cmovcc() {
    return Util.extractNBit(property, 25, 1) != 0;
  }

  public boolean jcc() {
    return Util.extractNBit(property, 26, 1) != 0;
  }

  public int stage() {
    return Util.extractNBit(property, 28, 4);
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

    if (hasSuffix()) {
      return mnemonics + suffix;
    } else {
      return mnemonics;
    }
  }
}

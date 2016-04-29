package edu.mit.compilers.codegen;

import edu.mit.compilers.common.Util;

public enum Op {

  /* Bit | usage
   * 0    Can be used as ISA Op
   * 1    Can be used as Pseudo Op
   * 2 3  ISA operand count (0-3)
   * 4-5  set if operand 1, 2, 3 is written to, or 0 none
   * 6-7  set if operand 1, 2 is read
   * 8    Non ISA dest is dummy? (either must be dummy or mustn't be)
   * 9 10 Non ISA operand count (0-3)
   * 11   set if additional reg is written to (call, idiv)
   * 12   has suffix
   * 13   has XMM form
   * 14   has CSE side effect / Not CSE-able
   * 15   has DSE side effect / NOT DSE-able
   *
   * 16 17 control flow: 0 = no jump, 1 = jcc; 2 = jmp; 3 = ret/noret
   * 18   invalidate flag
   * 19   set flag (implies 18)
   * 20   test flag
   * 21   communicative (ISA only) (op a, b == op b, a)
   * 24   setcc
   * 25   cmovcc
   * 26   jcc
   * 28-31 Pseudo op may not appear after this stage in fixed pipeline
   *                             C   8   4   0   C   8   4   0
   */
  ADD("add",                0b01110000001011000011010011101011),
  SUB("sub",                0b01110000000011000011010011101011),
  IMUL("imul",              0b01110000001001000011010011101011),
  XOR("xor",                0b01110000001011000011010011101011),
  SAL("sal",                0b01110000000011000011010011101011),
  SAR("sar",                0b01110000000011000011010011101011),

  IDIV("idiv",              0b01110000000001001001101101000111),

  NOT("not",                0b01110000000011000001001001010111),
  NEG("neg",                0b01110000000011000001001001010111),
  INC("inc",                0b01110000000011000001001001010111),
  DEC("dec",                0b01110000000011000001001001010111),

  MOV("mov",                0b01110000000000000011001001101011),
  MOVSBQ("movsbq",          0b00000000000000000000000001101001),
  LEA("lea",                0b01110000000000000001001001101011),

  PUSH("push",              0b00000000000000000001000001000101),
  POP("pop",                0b00000000000000000001000000010101),

  CMP("cmp",                0b01110000000011001001010111001011),
  TEST("test",              0b01110000001011001001010111001011),

  SETE("sete",              0b01110001000100000000000000010111),
  SETNE("setne",            0b01110001000100000000000000010111),
  SETG("setg",              0b01110001000100000000000000010111),
  SETGE("setge",            0b01110001000100000000000000010111),
  SETL("setl",              0b01110001000100000000000000010111),
  SETLE("setle",            0b01110001000100000000000000010111),

  CMOVE("cmove",            0b00000010000100000001000001101001),
  CMOVNE("cmovne",          0b00000010000100000001000001101001),
  CMOVG("cmovg",            0b00000010000100000001000001101001),
  CMOVGE("cmovge",          0b00000010000100000001000001101001),
  CMOVL("cmovl",            0b00000010000100000001000001101001),
  CMOVLE("cmovle",          0b00000010000100000001000001101001),

  JE("je",                  0b00000100000100010000010100000111),
  JNE("jne",                0b00000100000100010000010100000111),
  JG("jg",                  0b00000100000100010000010100000111),
  JGE("jge",                0b00000100000100010000010100000111),
  JL("jl",                  0b00000100000100010000010100000111),
  JLE("jle",                0b00000100000100010000010100000111),

  JMP("jmp",                0b00000000000000100000001100000111),
  CALL("call",              0b00000000000001001100100000000101),
  RET("ret",                0b00000000000000110000000000000001),

  CQO("cqo",                0b00000000000000000000100000000001),
  
  NOP("nop",                0b11110000000000000000000100000011),

  // Pseudo ops from midend, need to be lowered in backend
  //                             C   8   4   0   C   8   4   0
  LOCAL_ARRAY_DECL(
      "local_array",        0b11010000000000000001001000000010),
  FAKE_DIV("xdiv",          0b00000000000001001001110000000010), // d = a / b (div|mod)
  LOAD("load",              0b01110000000000000001010000000010), // d = a[b]
  STORE("store",            0b01110000000000001101010000000010), // a[b] = d
  FAKE_CALL("xcall",        0b00000000000001000001101000000010), // d = call $a (args...) #xmm used
  OUT_OF_BOUNDS(
      "out_of_bounds",      0b11110000000000000000010000000010), // d = funcName a = line, b = col
  CONTROL_REACHES_END(
      "control_reaches_end",0b11110000000000000000010000000010), // d = funcName a = line, b = col
  ALLOCATE("allocate",      0b11010000000000000000001100000010), // a = Imm64 (set up for func call)
  TEMP_REG("temp_reg",      0b10100000000000000001000000000010),
  END_TEMP_REG(
      "end_temp_reg",       0b10100000000000000000000100000010),
  BEGIN_XCALL("begin_xcall",0b10100000000000000000000100000010),
  BEGIN_XDIV("begin_xdiv",  0b10100000000000000000000100000010),
  END_XCALL("end_xcall",    0b10100000000000000000000100000010),
  END_XDIV("end_xdiv",      0b10100000000000000000000100000010),
  PROLOGUE("func_prologue", 0b11010000000000000000000100000010),
  GET_ARG("get_arg",        0b00000000000000000001001000000010), // d = var, a = Imm64 (index of arg)
  EPILOGUE("func_epilogue", 0b11010000000000000000000100000010),
  NO_RETURN("#no_ret",      0b11110000000000110000000100000011),
  DELETED("",               0b11110000000000000000000100000011)
  ;

  String mnemonics;
  int property;

  Op(String s, int property) {
    mnemonics = s;
    this.property = property;

    assert(isa() || pseudoOp());
    assert (Util.implies(isa() && !pseudoOp(), !pseudoOpDestMustBeDummy() && pseudoOpOperandNun() == 0));
    assert (Util.implies(!isa() && pseudoOp(), isaOperandNum() == 0 && isaWriteDest() == 0 && isaReadSrc() == 0));
    assert (isaOperandNum() <= 2);

    int readSrc = isaReadSrc();
    assert ((readSrc & 2) == 0 || isaOperandNum() == 2);
    assert ((readSrc & 1) == 0 || isaOperandNum() >= 1);

    assert (Util.implies(setFlag(), invalidateFlag()));
    assert (Util.implies(communicative(), isa() && isaOperandNum() == 2));
  }

  public boolean isa() {
    return Util.extractNBit(property, 0, 1) != 0;
  }

  public boolean pseudoOp() {
    return Util.extractNBit(property, 1, 1) != 0;
  }

  public int isaOperandNum() {
    return Util.extractNBit(property, 2, 2);
  }

  public int isaWriteDest() {
    return Util.extractNBit(property, 4, 2);
  }

  public int isaReadSrc() {
    return Util.extractNBit(property, 6, 2);
  }

  public boolean pseudoOpDestMustBeDummy() {
    return Util.extractNBit(property, 8, 1) != 0;
  }

  public int pseudoOpOperandNun() {
    return Util.extractNBit(property, 9, 2);
  }

  public boolean special() {
    return Util.extractNBit(property, 11, 1) != 0;
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

  public boolean communicative() {
    return Util.extractNBit(property, 21, 1) != 0;
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

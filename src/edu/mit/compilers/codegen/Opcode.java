package edu.mit.compilers.codegen;

public enum Opcode {

  // copying values
  MOV("mov"),
  CMOVE("cmove"),
  CMOVNE("comvne"),
  CMOVG("cmovg"),
  CMOVGE("cmovge"),
  CMOVL("cmovl"),
  CMOVLE("cmovle"),

  // stack management
  ENTER("enter"), // do not use
  LEAVE("leave"), // do not use
  PUSH("push"),
  POP("pop"),

  // control flow
  CALL("call"),
  RET("ret"),
  JMP("jmp"),
  JE("je"),
  JNE("jne"),
  JL("jl"),
  JLE("jle"),
  JG("jg"),
  JGE("jge"),

  // arithmetic and logic
  ADD("add"),
  SUB("sub"),
  IMUL("imul"),
  IDIV("idiv"),
  SHR("shr"),
  SHL("shl"),
  LEA("lea"),
  CMP("cmp"),
  TEST("test"),

  SETE("sete"),
  SETNE("setne"),
  SETGE("setge"),
  SETG("setg"),
  SETLE("setle"),
  SETL("setl"),

  AND("and"),
  OR("or"),
  XOR("xor"),

  //
  NOP("nop");

  private final String root;

  Opcode(String op) {
    this.root = op;
  }

  @Override
  public String toString() {
    return root;
  }

  public String toString(Value.OperandType registerType) {
    String suffix = "";
    switch (registerType) {
    case r8:
      suffix = "b";
      break;
    case r16:
      suffix = "w";
      break;
    case r32:
      suffix = "l";
      break;
    case r64:
      suffix = "q";
      break;
    }

    switch (this) {
    case MOV:
    case PUSH:
    case POP:
    case ADD:
    case SUB:
    case IMUL:
    case IDIV:
    case SHR:
    case SHL:
    case CMP:
    case TEST:
    case LEA:
    case AND:
    case OR:
    case XOR:
      return root + suffix;
    case ENTER:
    case LEAVE:
    case CALL:
    case RET:
    case JMP:
    case JE:
    case JNE:
    case JG:
    case JGE:
    case JL:
    case JLE:
    case SETE:
    case SETNE:
    case SETG:
    case SETGE:
    case SETL:
    case SETLE:
    case NOP:
      return root;
    }
    return "";
  }
}

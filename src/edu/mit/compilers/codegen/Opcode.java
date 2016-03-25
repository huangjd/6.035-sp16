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


  //	private String getAssemblySuffix(Value.OperandType registerType) {
  //		switch (registerType)registerType {
  //		case Value.OperandType.r8:
  //			return "B";
  //			break;
  //		case Value.OperandType.r16:
  //			return "W";
  //			break;
  //		case Value.OperandType.r32:
  //			return "L";
  //			break;
  //
  //		}
  //	}

  Opcode(String op) {
    this.root = op;
  }

  @Override
  public String toString() {
    return root;
  }

  public String toString(Value.OperandType registerType) {
    switch (this) {
    case MOV:
      if (registerType == Value.OperandType.r8) {
        return "MOVB";
      } else if (registerType == Value.OperandType.r16){
        return "MOVW";
      } else if (registerType == Value.OperandType.r32) {
        return "MOVL";
      } else if (registerType == Value.OperandType.r64) {
        return "MOVQ";
      } else if (registerType == Value.OperandType.xmm) {
        return "MOVDQA";
      }
    case ENTER:
      return "ENTER";
    case LEAVE:
      return "LEAVE";
    case PUSH:
      if (registerType == Value.OperandType.r16){
        return "PUSHW";
      } else if (registerType == Value.OperandType.r32) {
        return "PUSHL";
      } else if (registerType == Value.OperandType.r64) {
        return "PUSHQ";
      }
    case POP:
      if (registerType == Value.OperandType.r16){
        return "POPW";
      } else if (registerType == Value.OperandType.r32) {
        return "POPL";
      } else if (registerType == Value.OperandType.r64) {
        return "POPQ";
      }
    case CALL:
      return "CALL";
    case RET:
      return "RET";
    case JMP:
      return "JMP";
    case JE:
      return "JE";
    case JNE:
      return "JNE";
    case ADD:
      if (registerType == Value.OperandType.r8) {
        return "ADDB";
      } else if (registerType == Value.OperandType.r16){
        return "ADDW";
      } else if (registerType == Value.OperandType.r32) {
        return "ADDL";
      } else if (registerType == Value.OperandType.r64) {
        return "ADDQ";
      }
    case SUB:
      if (registerType == Value.OperandType.r8) {
        return "SUBB";
      } else if (registerType == Value.OperandType.r16){
        return "SUBW";
      } else if (registerType == Value.OperandType.r32) {
        return "SUBL";
      } else if (registerType == Value.OperandType.r64) {
        return "SUBQ";
      }
    case IMUL:
      if (registerType == Value.OperandType.r8) {
        return "IMULB";
      } else if (registerType == Value.OperandType.r16){
        return "IMULW";
      } else if (registerType == Value.OperandType.r32) {
        return "IMULL";
      } else if (registerType == Value.OperandType.r64) {
        return "IMULQ";
      }
    case IDIV:
      if (registerType == Value.OperandType.r8) {
        return "IDIVB";
      } else if (registerType == Value.OperandType.r16){
        return "IDIVW";
      } else if (registerType == Value.OperandType.r32) {
        return "IDIVL";
      } else if (registerType == Value.OperandType.r64) {
        return "IDIVQ";
      }
    case SHR:
      if (registerType == Value.OperandType.r8) {
        return "SHRB";
      } else if (registerType == Value.OperandType.r16){
        return "SHRW";
      } else if (registerType == Value.OperandType.r32) {
        return "SHRL";
      } else if (registerType == Value.OperandType.r64) {
        return "SHRQ";
      }
    case SHL:
      if (registerType == Value.OperandType.r8) {
        return "SHLB";
      } else if (registerType == Value.OperandType.r16){
        return "SHLW";
      } else if (registerType == Value.OperandType.r32) {
        return "SHLL";
      } else if (registerType == Value.OperandType.r64) {
        return "SHLQ";
      }
    case CMP:
      if (registerType == Value.OperandType.r8) {
        return "CMPB";
      } else if (registerType == Value.OperandType.r16){
        return "CMPW";
      } else if (registerType == Value.OperandType.r32) {
        return "CMPL";
      } else if (registerType == Value.OperandType.r64) {
        return "CMPQ";
      }
    case SETGE:
      return "SETGE";
    case SETG:
      return "SETG";
    case SETL:
      return "SETL";
    case SETLE:
      return "SETLE";
    case SETNE:
      return "SETNE";
    case AND:
      if (registerType == Value.OperandType.r8) {
        return "ANDB";
      } else if (registerType == Value.OperandType.r16){
        return "ANDW";
      } else if (registerType == Value.OperandType.r32) {
        return "ANDL";
      } else if (registerType == Value.OperandType.r64) {
        return "ANDQ";
      }
    case OR:
      if (registerType == Value.OperandType.r8) {
        return "ORB";
      } else if (registerType == Value.OperandType.r16){
        return "ORW";
      } else if (registerType == Value.OperandType.r32) {
        return "ORL";
      } else if (registerType == Value.OperandType.r64) {
        return "ORQ";
      }
    case XOR:
      if (registerType == Value.OperandType.r8) {
        return "XORB";
      } else if (registerType == Value.OperandType.r16){
        return "XORW";
      } else if (registerType == Value.OperandType.r32) {
        return "XORL";
      } else if (registerType == Value.OperandType.r64) {
        return "XORQ";
      }
    default:
      return "";
    }
  }
}

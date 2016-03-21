package edu.mit.compilers.codegen;

public enum Opcode {
  // copying values
  MOV,
  CMOVE,
  CMOVNE,
  CMOVG,
  CMOVL,
  CMOVGE,
  CMOVLE,

  // stack management
  ENTER,
  LEAVE,
  PUSH,
  POP,

  // control flow
  CALL,
  RET,
  JMP,
  JE,
  JNE,

  // arithmetic and logic
  ADD,
  SUB,
  IMUL,
  IDIV,
  SHR,
  SHL,
  ROR,
  CMP,
  SETGE,
  SETG,
  SETLE,
  SETL,
  SETE,
  SETNE,

  AND,
  OR,
  XOR,
  NAND,
  NOR,

  //
  NOP;
	
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
		case ROR:
			if (registerType == Value.OperandType.r8) {
				return "RORB";
			} else if (registerType == Value.OperandType.r16){
				return "RORW";
			} else if (registerType == Value.OperandType.r32) {
				return "RORL";
			} else if (registerType == Value.OperandType.r64) {
				return "RORQ";
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
			break;
		}
	}
}



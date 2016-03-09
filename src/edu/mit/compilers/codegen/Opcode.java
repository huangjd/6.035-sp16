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
	CMP
}



package edu.mit.compilers.common;

import edu.mit.compilers.codegen.*;

public class OpcodeException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
	public final Opcode op;
	
	public OpcodeException(Opcode op) {
		this.op = op;
		// TODO add more info
	}
	
}

package edu.mit.compilers.codegen;

import java.util.*;

import edu.mit.compilers.nodes.Function;

public class IRBuilder {

	BasicBlock currentBB;
	Function currentFunction;

	ArrayList<ArrayList<BasicBlock>> functions;

	public IRBuilder() {
		currentFunction = null;
		functions = new ArrayList<>();
	}

	public BasicBlock beginFunction(Function f) {
		if (currentFunction != null) {
			throw new RuntimeException("Called IRBuilder::beginFunction while compiling a function");
		}
		BasicBlock newbb = new BasicBlock();
		ArrayList<BasicBlock> temp = new ArrayList<>();
		temp.add(newbb);
		functions.add(temp);
		currentFunction = f;
		currentBB = newbb;
		return newbb;
	}

	public void endFunction() {
		if (currentFunction == null) {
			throw new RuntimeException("Called IRBuilder::endFunction while not compiling a function");
		}
		currentFunction = null;
	}

	public BasicBlock createBasicBlock() {
		BasicBlock bb = new BasicBlock();
		functions.get(functions.size() - 1).add(bb);
		return bb;
	}

	public void setCurrentBasicBlock(BasicBlock bb) {
		currentBB = bb;
	}

	public BasicBlock getCurrentBasicBlock() {
		return currentBB;
	}

	public Register emitOp(Opcode op, Register a, Register b) {

	}

	public Register emitOp(Opcode op, Register a) {

	}

	public Register emitOp(Opcode op, Register a, Immediate b) {

	}

	public Register emitOp(Opcode op, Register a, Register base, Register index, long offset) {

	}

	public AbstractMap.SimpleEntry<Register, Register> emitDiv(Register a, Register b) {

	}

	public Register emitLoad(Register base, Register index, long offset) {

	}

	public void emitStore(Register value, Register base, Register index, long offset) {

	}

























}

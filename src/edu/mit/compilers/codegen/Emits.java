package edu.mit.compilers.codegen;

import edu.mit.compilers.common.OpcodeException;
public class Emits {

	private static void appendToCurrentBlock(Instruction next) {
		BasicBlock currentBlock = IRBuilder.getCurrentBasicBlock();
		currentBlock.seq.add(next);
	}

	public static Register emitAdd(Register a, Register b) {
		Register result = new Register(a.value + b.value);
		appendToCurrentBlock(new Instruction(Opcode.ADD, result));
		return result;
	}

	public static Register emitAnd(Register a, Register b) {
		return null;
	}

	public static Register emitAssign(Register a, Register b) {
		return null;
	}

	public static Register emitBlock(Register a, Register b) {
		return null;
	}

	public static Register emitBooleanLiteral(Register a) {
		return null;
	}

	public static void emitBreak() {

	}

	public static void emitContinue() {

	}

	public static void emitDie() {

	}

	public static Register emitDiv(Register a, Register b) {
		return null;
	}

	public static Register emitEq(Register a) {
		return null;
	}

	public static Register emitFor(Register a) {
		return null;
	}

	public static Register emitGe(Register a, Register b) {
		return null;
	}

	public static Register emitGt(Register a) {
		return null;
	}

	public static Register emitIf(Register a) {
		return null;
	}

	public static Register emitIntLiteral(Register a) {
		return null;
	}

	public static Register emitLe(Register a) {
		return null;
	}
	public static Register emitLength(Register a) {
		return null;
	}

	public static Register emitLoad(Register a) {
		return null;
	}

	public static Register emitLt(Register a) {
		return null;
	}

	public static Register emitMinus(Register a) {
		return null;
	}

	public static Register emitMod(Register a, Register b) {
		return null;
	}

	public static Register emitMul(Register a, Register b) {
		return null;
	}

	public static Register emitNe(Register a, Register b) {
		return null;
	}

	public static Register emitNot(Register a) {
		return null;
	}

	public static Register emitOr(Register a) {
		return null;
	}

	public static Register emitPass(Register a) {
		return null;
	}

	public static Register emitReturn(Register a) {
		return null;
	}

	public static Register emitStore(Register a) {
		return null;
	}

	public static Register emitStringLiteral(Register a) {
		return null;
	}

	public static Register emitSub(Register a, Register b) {
		return null;
	}

	public static Register emitTernary(Register a, Register b, Register c) {
		return null;
	}

	public static Register emitUnparsedIntLiteral(Register a) {
		return null;
	}

	public static Register emitWhile(Register a) {
		return null;
	}




}

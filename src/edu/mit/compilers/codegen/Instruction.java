package edu.mit.compilers.codegen;

public class Instruction {

  public Opcode op;
  public int addressingmode;
  public Register dest, a, b, c;

  public Instruction(Opcode op, Register dest, Register a, Register b) {
    this.op = op;
    this.dest = dest;
    this.a = a;
    this.b = b;
  }

  public Instruction(Opcode op, Register dest, Register a, Register b, Register c) {
    this.op = op;
    this.dest = dest;
    this.a = a;
    this.b = b;
    this.c = c;
  }
}

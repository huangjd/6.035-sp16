package edu.mit.compilers.codegen;

public class Instruction {

  boolean isa;
  public Opcode op;
  public Value dest, a, b, c;

  public Instruction(Value dest, Opcode op, Value a) {
    this(dest, op, a, null, null);
  }

  public Instruction(Value dest, Opcode op, Value a, Value b) {
    this(dest, op, a, b, null);
  }

  public Instruction(Value dest, Opcode op, Value a, Value b, Value c) {
    assert (dest instanceof Register || dest instanceof Memory);
    this.isa = false;
    this.op = op;
    this.dest = dest;
    this.a = a;
    this.b = b;
    this.c = c;
  }

  public Instruction(int dummy, Opcode op, Value a) {
    this.isa = false;
    this.op = op;
    this.a = a;
  }

  public Instruction(int dummy, Opcode op, Value a, Value b) {
    this.isa = false;
    this.op = op;
    this.a = a;
    this.b = b;
  }

  // ISA form instruction, need explicit check
  public Instruction(Opcode op) {

  }

  public Instruction(Opcode op, Value a) {
  }

  public Instruction(Opcode op, Value a, Value b) {
  }

  public Instruction(Opcode op, Value a, Value b, Value c) {
  }
  
 
  
}

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
    assert (dest.value instanceof Register || dest.value instanceof Memory);
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
    this(op, null, null, null);
  }

  public Instruction(Opcode op, Value a) {
    this(op, a, null, null);
  }

  public Instruction(Opcode op, Value a, Value b) {
    this(op, a, b, null);
  }

  public Instruction(Opcode op, Value a, Value b, Value c) {
    this.isa = true;
    this.op = op;
    this.a = a;
    this.b = b;
    this.c = c;
  }
  
  public 

  @Override
  public String toString() {
    return (dest != null && !isa ? dest.toString() + " = " : "") +
        op.toString() +
        (a != null ? " " + a.toString() : "") +
        (b != null ? " ," + b.toString() : "") +
        (c != null ? " ," + c.toString() : "");
  }
}

class Label extends Instruction {
  String symbol;

  public Label(String s) {
    super(null);
    symbol = s;
  }

  @Override
  public String toString() {
    return symbol + ":";
  }
  
 
  
}

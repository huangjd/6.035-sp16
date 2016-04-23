package edu.mit.compilers.codegen;

import java.util.HashSet;

public class RegisterUsage extends BasicBlockVisitor<RegisterUsage.T> {

  boolean omitrbp;

  public RegisterUsage(boolean omitrbp) {
    this.omitrbp = omitrbp;
  }

  static class T implements Transformable<T> {
    final int val;

    T(int val) {
      this.val = val;
    }

    @Override
    public T transform(T t) {
      return new T(val | t.val);
    }

  }

  @Override
  public T getInitValue() {
    return new T(0);
  }
/*
  @Override
  public void fini(HashSet<BasicBlock> exits) {
    
    
    
    switch (ins.op) {
    case END_XCALL:
      for (int j = i - 1; j >= 0; j--) {
        Instruction ins2 = b.get(j);
        if (ins2.op == Op.CALL) {
          int avail = ~(out | (1 << Register.rax.id |
              1 << Register.rcx.id |
              1 << Register.rdx.id |
              1 << Register.rdi.id |
              1 << Register.rsi.id |
              1 << Register.rsp.id |
              (omitrbp ? 0 : (1 << Register.rbp.id)) |
              1 << Register.r8.id |
              1 << Register.r9.id |
              1 << Register.r10.id |
              1 << Register.r11.id));
          int mustSave = out & (1 << Register.rcx.id |
              1 << Register.rdx.id |
              1 << Register.rdi.id |
              1 << Register.rsi.id |
              1 << Register.r8.id |
              1 << Register.r9.id |
              1 << Register.r10.id |
              1 << Register.r11.id);

          break;
        }
      }
      throw new RuntimeException();

    case END_XDIV:

      break;
      out &= ~(1 << Register.rax.id);
      out &= ~(1 << Register.rdx.id);
      if (ins.a instanceof Register) {
        out |= (1 << ((Register) ins.a).id);
      }
      break;
    default:
      for (Operand dest : ins.getDest()) {
        for (Register reg : Register.getRegistersReferedByOperand(dest)) {
          out &= ~(1 << reg.id);
        }
      }

      for (Register reg : Register.getRegistersReferedByOperand(ins.a)) {
        out |= (1 << reg.id);
      }
      for (Register reg : Register.getRegistersReferedByOperand(ins.b)) {
        out |= (1 << reg.id);
      }
      break;
    case FAKE_CALL:
    case FAKE_DIV:
      throw new RuntimeException();
    }
  }
*/
  @Override
  public T visit(BasicBlock b, T in) {
    int out = in.val;
    for (int i = b.size() - 1; i >= 0; i--) {
      Instruction ins = b.get(i);
      switch (ins.op) {
      case IDIV:
        out &= ~(1 << Register.rax.id);
        out &= ~(1 << Register.rdx.id);
        for (Register reg : Register.getRegistersReferedByOperand(ins.a)) {
          out |= (1 << reg.id);
        }
        break;
      default:
        for (Operand dest : ins.getDest()) {
          for (Register reg : Register.getRegistersReferedByOperand(dest)) {
            out &= ~(1 << reg.id);
          }
        }

        for (Register reg : Register.getRegistersReferedByOperand(ins.a)) {
          out |= (1 << reg.id);
        }
        for (Register reg : Register.getRegistersReferedByOperand(ins.b)) {
          out |= (1 << reg.id);
        }
        break;
      case FAKE_CALL:
      case FAKE_DIV:
        throw new RuntimeException();
      }
    }
    return new T(out);
  }
}

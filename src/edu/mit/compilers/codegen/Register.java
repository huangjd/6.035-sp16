package edu.mit.compilers.codegen;

public class Register extends Operand {

  public final int id;
  public final Type type;
  public final String name;

  public static final Register
  orbp = new Register(4, Type.r64, "rbp"), // used for omit-frame-pointer
  rip = new Register(-1, Type.r64, "rip"),
  rax = new Register(0, Type.r64, "rax"),
  rcx = new Register(1, Type.r64, "rcx"),
  rdx = new Register(2, Type.r64, "rdx"),
  rbx = new Register(3, Type.r64, "rbx"),
  rbp = new Register(4, Type.r64, "rbp"),
  rsp = new Register(5, Type.r64, "rsp"),
  rdi = new Register(6, Type.r64, "rdi"),
  rsi = new Register(7, Type.r64, "rsi"),
  r8 = new Register(8, Type.r64, "r8"),
  r9 = new Register(9, Type.r64, "r9"),
  r10 = new Register(10, Type.r64, "r10"),
  r11 = new Register(11, Type.r64, "r11"),
  r12 = new Register(12, Type.r64, "r12"),
  r13 = new Register(13, Type.r64, "r13"),
  r14 = new Register(14, Type.r64, "r14"),
  r15 = new Register(15, Type.r64, "r15"),

  al = new Register(0, Type.r8, "al"),
  cl = new Register(1, Type.r8, "cl"),
  dl = new Register(2, Type.r8, "dl"),
  bl = new Register(3, Type.r8, "bl"),
  bpl = new Register(4, Type.r8, "bpl"),
  spl = new Register(5, Type.r8, "spl"),
  dil = new Register(6, Type.r8, "dil"),
  sil = new Register(7, Type.r8, "sil"),
  r8b = new Register(8, Type.r8, "r8b"),
  r9b = new Register(9, Type.r8, "r9b"),
  r10b = new Register(10, Type.r8, "r10b"),
  r11b = new Register(11, Type.r8, "r11b"),
  r12b = new Register(12, Type.r8, "r12b"),
  r13b = new Register(13, Type.r8, "r13b"),
  r14b = new Register(14, Type.r8, "r14b"),
  r15b = new Register(15, Type.r8, "r15b");

  Register(int id, Type type, String name) {
    this.id = id;
    this.type = type;
    this.name = name;
  }

  public static Register[] getRegistersReferedByOperand(Operand op) { // except rip, because you don't do analysis on rip
    if (op instanceof Register && op != rip) {
      return new Register[]{(Register) op};
    } else if (op instanceof Memory) {
      Memory mem = (Memory) op;
      if (mem.base != rip) {
        if (mem.index != null && mem.index != rip) {
          return new Register[]{mem.base, mem.index};
        } else {
          return new Register[]{mem.base};
        }
      } else {
        if (mem.index != null && mem.index != rip) {
          return new Register[]{mem.index};
        }
      }
    }
    return new Register[]{};
  }

  @Override
  public Type getType() {
    return type;
  }

  @Override
  public String toString() {
    return "%" + name;
  }

  @Override
  public boolean isPointer() {
    return false;
  }
}

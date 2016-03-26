package edu.mit.compilers.codegen;

import java.io.*;
import java.util.HashMap;

import edu.mit.compilers.codegen.Backend.FunctionContent;
import edu.mit.compilers.common.*;
import edu.mit.compilers.nodes.ProgramNode;
import edu.mit.compilers.tools.CLI;
import edu.mit.compilers.visitors.IRPrinter;

public class Compile {

  static StringBuilder debug;

  public static void compile(ProgramNode program, PrintStream codeOutput, PrintStream debugOutput) {
    debug = new StringBuilder();

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    IRPrinter p = new IRPrinter(new PrintStream(baos));
    p.enter(program);
    debug.append("----------" + " IR  " + "----------\n");
    debug.append(baos.toString());

    Backend backend = new Backend(CLI.outfile);
    backend.enter(program);

    debug.append("\n----------" + " SSA " + "----------\n");
    for (FunctionContent functionContent : backend.functions) {
      for (BasicBlock bb : functionContent.text) {
        debug.append(bb.toString());
      }
      debug.append('\n');
    }

    debug.append("\n----------" + " ASM lower Virtual Reg " + "----------\n");

    for (FunctionContent func : backend.functions) {
      Allocator alloc = new UnoptimizedAllocator();
      long offset = alloc.transform(func.text, func.baseStackAdjust);
      func.baseStackAdjust = offset;
      BasicBlock first = func.text.get(0);
      first.seq.add(Math.max(0, first.seq.size() - 1),
          new Instruction(Opcode.ADD, new Immediate(-offset).box(), Register.RSP.box()));

      for (BasicBlock bb : func.text) {
        String s = bb.toString();
        debug.append(s);
        // codeOutput.print(s);
      }
      // codeOutput.print('\n');
      debug.append('\n');
    }

    debug.append("\n----------" + " ASM lower Op Mem, Mem " + "----------\n");
    for (FunctionContent func : backend.functions) {
      Allocator alloc = new LowerMemMem();
      long offset = alloc.transform(func.text, func.baseStackAdjust);

      for (BasicBlock bb : func.text) {
        String s = bb.toString();
        debug.append(s);
        codeOutput.print(s);
      }
      codeOutput.print('\n');
      debug.append('\n');
    }

    ScopedMap<Var, Value> symtab = backend.symtab;
    for (Var v : symtab.keySet()) {
      String id = ".bss." + v.id;
      String bssdecl = ".globl\t" + id + "\n" +
          "\t.bss\n" +
          "\t.align 16\n" +
          "\t.type\t" + id + ", @object\n" +
          "\t.size\t" + id + ", " + Long.toString(v.size) + "\n" +
          id + ":\n" +
          "\t.zero\t" + Long.toString(v.size) + "\n";
      codeOutput.print(bssdecl);
      debug.append(bssdecl);
    }

    HashMap<String, Value> strtab = backend.strtab;
    for (String id : strtab.keySet()) {
      String name = strtab.get(id).toString().substring(1);

      String strtabdecl = ".globl\t" + name + "\n" +
          "\t.section\t.rodata\n" +
          name + ":\n" +
          "\t.string\t" + id + "\n";
      codeOutput.print(strtabdecl);
      debug.append(strtabdecl);
    }

    if (CLI.debug) {
      debugOutput.print(debug.toString());
    }
  }
}
package edu.mit.compilers;

import java.io.*;

import edu.mit.compilers.codegen.*;
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

    debug.append("\n----------" + " ASM " + "----------\n");
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

    SymbolTable strtab = backend.strtab;
    for (String id : strtab.keySet()) {
      String strtabdecl = ".globl\t" + id + "\n" +
          "\t.section\t.rodata" +
          id + ":\n" +
          "\t.string\t\"" + strtab.lookup(id) + "\"\n";
      codeOutput.print(strtabdecl);
      debug.append(strtabdecl);
    }

    for (FunctionContent func : backend.functions) {
      Allocator alloc = new Allocater();
      int offset = alloc.transform(func.text);
      offset += func.baseStackAdjust;
      BasicBlock first = func.text.get(0);
      first.seq.add(Math.max(0, first.seq.size() - 1),
          new Instruction(Opcode.ADD, new Immediate(-offset).box(), Register.RSP.box()));

      for (BasicBlock bb : func.text) {
        String s = bb.toString();
        debug.append(s);
        codeOutput.print(s);
      }
      codeOutput.print('\n');
      debug.append('\n');
    }

    if (CLI.debug) {
      debugOutput.print(debug.toString());
    }
  }
}

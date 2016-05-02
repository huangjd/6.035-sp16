package edu.mit.compilers;

import java.io.*;

import antlr.Token;
import edu.mit.compilers.codegen.*;
import edu.mit.compilers.common.ErrorLogger;
import edu.mit.compilers.grammar.*;
import edu.mit.compilers.nodes.ProgramNode;
import edu.mit.compilers.tools.CLI2;
import edu.mit.compilers.visitors.IRPrinter;

class Main {

  static CLI2 CLI = new CLI2();

  static CFG backend(CFG ir) {
    new UnreachableBlockRemoval().traverse(ir);


    if (CLI.opts[CLI2.Optimization.CSE.index]) {
      new CSE().traverse(ir);
      if (CLI.debug) {
        System.out.println("----- CSE -----");
        System.out.print(ir.toString());
      }
    }

    new LowerPseudoOp1().traverse(ir);
    if (CLI.debug) {
      System.out.println("----- IR lower pseudo op I -----");
      System.out.print(ir.toString());
    }

    /*
     * if (CLI.opts[CLI2.Optimization.CSE.index]) {
     * new CSEPass().traverse(ir);
     * if (CLI.debug) {
     * System.out.println("----- CSE -----");
     * System.out.print(ir.toString());
     * }
     * }
     *
     * if (CLI.opts[CLI2.Optimization.CP.index]) {
     * new CPPass().traverse(ir);
     * if (CLI.debug) {
     * System.out.println("----- CP -----");
     * System.out.print(ir.toString());
     * }
     * }
     */

    /*
     * if (CLI.opts[CLI2.Optimization.REGALLOC.index]) {
     * new
     * RegisterAllocator(CLI.opts[CLI2.Optimization.OMITRBP.index]).traverse(ir)
     * ;
     * if (CLI.debug) {
     * System.out.println("----- regalloc -----");
     * System.out.print(ir.toString());
     * }
     * }
     */

    new BasicStackAllocator().traverse(ir);
    if (CLI.debug) {
      System.out.println("----- IR allocate stack vars -----");
      System.out.print(ir.toString());
    }
    new Lower3Operand().traverse(ir);
    if (CLI.debug) {
      System.out.println("----- IR lower 3 operands -----");
      System.out.print(ir.toString());
    }
    new ResolveTempReg().reverseTraverse(ir);
    if (CLI.debug) {
      System.out.println("----- IR resolve temp regs -----");
      System.out.print(ir.toString());
    }
    new StackFrameSetup(CLI.opts[CLI2.Optimization.OMITRBP.index]).traverse(ir);
    if (CLI.debug) {
      System.out.println("----- IR set up stack frame -----");
      System.out.print(ir.toString());
    }
    new LowerPseudoOp2(CLI.opts[CLI2.Optimization.PEEPHOLE.index]).traverse(ir);
    if (CLI.debug) {
      System.out.println("----- IR lower pseudo op II -----");
      System.out.print(ir.toString());
    }
    // new Linearizer().traverse(ir);
    return ir;
  }

  static CFG midend(ProgramNode program) {
    Midend visitor = new Midend(CLI.infile);
    visitor.enter(program);
    CFG ir = visitor.cfg;
    if (CLI.debug) {
      System.out.println("----- low level IR -----");
      System.out.print(ir.toString());
    }
    return ir;
  }

  public static void main(String[] args) {
    try {
      CLI.parse(args, new String[0]);
      InputStream inputStream = args.length == 0 ?
          System.in : new java.io.FileInputStream(CLI.infile);
      PrintStream outputStream = CLI.outfile == null ? System.out : new java.io.PrintStream(new java.io.FileOutputStream(CLI.outfile));

      String filaName = CLI.infile.substring(CLI.infile.lastIndexOf('/') + 1);
      if (CLI.target == CLI2.Action.SCAN) {
        DecafScanner scanner =
            new DecafScanner(new DataInputStream(inputStream));
        scanner.setTrace(CLI.debug);
        Token token;
        boolean done = false;
        while (!done) {
          try {
            for (token = scanner.nextToken();
                token.getType() != DecafParserTokenTypes.EOF;
                token = scanner.nextToken()) {
              String type = "";
              String text = token.getText();
              switch (token.getType()) {
              // TODO: add strings for the other types here...
              case DecafScannerTokenTypes.CHARLITERAL:
                type = " CHARLITERAL";
                break;
              case DecafScannerTokenTypes.INTLITERAL:
                type = " INTLITERAL";
                break;
              case DecafScannerTokenTypes.TK_true:
              case DecafScannerTokenTypes.TK_false:
                type = " BOOLEANLITERAL";
                break;
              case DecafScannerTokenTypes.STRINGLITERAL:
                type = " STRINGLITERAL";
                break;
              case DecafScannerTokenTypes.ID:
                type = " IDENTIFIER";
                break;
              }
              outputStream.println(token.getLine() + type + " " + text);
            }
            done = true;
          } catch(Exception e) {
            // print the error:
            System.err.println(filaName + " " + e);
            scanner.consume();
          }
        }
      } else if (CLI.target == CLI2.Action.PARSE ||
          CLI.target == CLI2.Action.DEFAULT) {
        DecafScanner scanner =
            new DecafScanner(new DataInputStream(inputStream));
        DecafParser parser = new DecafParser(scanner);
        parser.setTrace(CLI.debug);
        parser.program();
        if(parser.getError()) {
          System.exit(1);
        }
      } else if (CLI.target == CLI2.Action.INTER) {
        DecafScanner scanner =
            new DecafScanner(new DataInputStream(inputStream));
        DecafParser parser = new DecafParser(scanner);
        parser.setFilename(filaName);
        ProgramNode program = parser.program();
        ErrorLogger.printErrors();
        if (program == null || ErrorLogger.log.errors.size() > 0 || parser.getError()) {
          System.exit(-1);
        }
        if (CLI.debug) {
          IRPrinter p = new IRPrinter();
          p.printSymtab = true;
          p.enter(program);
        }
      } else if (CLI.target == CLI2.Action.ASSEMBLY) {
        DecafScanner scanner = new DecafScanner(new DataInputStream(inputStream));
        DecafParser parser = new DecafParser(scanner);
        parser.setFilename(filaName);
        ProgramNode program = parser.program();
        ErrorLogger.printErrors();
        if (program == null || ErrorLogger.log.errors.size() > 0 || parser.getError()) {
          System.exit(-1);
        }
        if (CLI.debug) {
          System.out.println("----- AST IR ------");
          IRPrinter p = new IRPrinter();
          p.printSymtab = true;
          p.enter(program);
        }
        CFG ir = midend(program);
        CFG done = backend(ir);
        outputStream.print(done.toString());
        outputStream.close();
      }
    } catch (Exception e) {
      // print the error:
      e.printStackTrace();
      System.err.println(CLI.infile + " " + e);
      System.exit(-1);
    }
  }
}

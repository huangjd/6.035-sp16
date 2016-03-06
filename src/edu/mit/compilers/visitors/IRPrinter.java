package edu.mit.compilers.visitors;

import java.io.PrintStream;
import java.util.*;

import edu.mit.compilers.common.Var;
import edu.mit.compilers.nodes.*;

public class IRPrinter extends Visitor {

  private int indent = 0;
  private PrintStream out;
  public boolean printSymtab = false;

  public IRPrinter() {
    out = System.out;
  }

  public IRPrinter(PrintStream out) {
    this.out = out;
  }

  void indent() {
    indent++;
  }

  void unindent() {
    indent--;
    if (indent < 0) {
      throw new RuntimeException();
    }
  }

  void printindent() {
    for (int i = 0; i < indent; i++) {
      out.print('\t');
    }
  }

  void printVar(Var var) {
    if (var.isPrimitive()) {
      out.printf("%s %s;", var.type.toString(), var.id);
    } else if (var.isArray()) {
      out.printf("%s %s[%d];", var.type.getElementType().toString(), var.id, var.length);
    }
  }

  @Override
  protected void visit(Add node) {
    out.print("(");
    node.left.accept(this);
    out.print(" + ");
    node.right.accept(this);
    out.print(")");
  }

  @Override
  protected void visit(Sub node) {
    out.print("(");
    node.left.accept(this);
    out.print(" - ");
    node.right.accept(this);
    out.print(")");
  }

  @Override
  protected void visit(IntLiteral node) {
    out.print(node.value);
  }

  @Override
  protected void visit(Length node) {
    out.print("@");
    out.print(node.array);
  }

  @Override
  protected void visit(Load node) {
    out.print(node.array);
    out.print("[");
    node.index.accept(this);
    out.print("]");
  }

  @Override
  protected void visit(Store node) {
    printindent();
    out.print(node.array);
    out.print("[");
    node.index.accept(this);
    out.print("] = ");
    node.value.accept(this);
    out.print(";\n");
  }

  @Override
  protected void visit(And node) {
    out.print("(");
    node.left.accept(this);
    out.print(" && ");
    node.right.accept(this);
    out.print(")");
  }

  @Override
  protected void visit(Not node) {
    out.print("!");
    node.right.accept(this);
  }

  @Override
  protected void visit(Or node) {
    out.print("(");
    node.left.accept(this);
    out.print(" || ");
    node.right.accept(this);
    out.print(")");
  }


  @Override
  protected void visit(Ternary node) {
    out.print("(");
    node.cond.accept(this);
    out.print(" ? ");
    node.trueExpr.accept(this);
    out.print(" : ");
    node.falseExpr.accept(this);
    out.print(")");
  }

  @Override
  protected void visit(Div node) {
    out.print("(");
    node.left.accept(this);
    out.print(" / ");
    node.right.accept(this);
    out.print(")");
  }

  @Override
  protected void visit(Eq node) {
    out.print("(");
    node.left.accept(this);
    out.print(" == ");
    node.right.accept(this);
    out.print(")");
  }

  @Override
  protected void visit(Ge node) {
    out.print("(");
    node.left.accept(this);
    out.print(" >= ");
    node.right.accept(this);
    out.print(")");
  }

  @Override
  protected void visit(Gt node) {
    out.print("(");
    node.left.accept(this);
    out.print(" > ");
    node.right.accept(this);
    out.print(")");
  }

  @Override
  protected void visit(Le node) {
    out.print("(");
    node.left.accept(this);
    out.print(" <= ");
    node.right.accept(this);
    out.print(")");
  }

  @Override
  protected void visit(Lt node) {
    out.print("(");
    node.left.accept(this);
    out.print(" < ");
    node.right.accept(this);
    out.print(")");
  }

  @Override
  protected void visit(Ne node) {
    out.print("(");
    node.left.accept(this);
    out.print(" != ");
    node.right.accept(this);
    out.print(")");
  }

  @Override
  protected void visit(Mul node) {
    out.print("(");
    node.left.accept(this);
    out.print(" * ");
    node.right.accept(this);
    out.print(")");
  }

  @Override
  protected void visit(Mod node) {
    out.print("(");
    node.left.accept(this);
    out.print(" % ");
    node.right.accept(this);
    out.print(")");
  }

  @Override
  protected void visit(Minus node) {
    out.print("-");
    node.right.accept(this);
  }

  @Override
  protected void visit(Assign node) {
    printindent();
    out.print(node.var);
    out.print(" = ");
    node.value.accept(this);
    out.print(";\n");
  }

  @Override
  protected void visit(Block node) {
    indent();
    if (printSymtab) {
      ArrayList<Var> locals = node.localSymbolTable.asList();
      for (Var local : locals) {
        printindent();
        out.print("// ");
        printVar(local);
        out.printf("\tLocal offset = %d\t%s\n", local.getStackOffset(),
            local.registerIndex == -1 ? "" : "register index = " + String.valueOf(local.registerIndex));
      }
    }

    for (StatementNode stmt : node.statements) {
      stmt.accept(this);
    }
    unindent();
  }

  @Override
  protected void visit(BooleanLiteral node) {
    out.print(node.value);
  }

  @Override
  protected void visit(Break node) {
    printindent();
    out.print("break;\n");
  }

  @Override
  protected void visit(Call node) {
    out.print(node.func.id);

    out.print("(");
    if (node.args.size() != 0) {
      for (int i = 0; i < node.args.size() - 1; i++) {
        node.args.get(i).accept(this);
        out.print(", ");
      }
      node.args.get(node.args.size() - 1).accept(this);
    }
    out.print(")");
  }

  @Override
  protected void visit(CallStmt node) {
    printindent();
    visit(node.call);
    out.print(";\n");
  }

  @Override
  protected void visit(Continue node) {
    printindent();
    out.print("continue;\n");
  }

  @Override
  protected void visit(Die node) {
    printindent();
    out.print("// exit(");
    out.print(node.exitCode);
    out.print(");\n");
  }

  @Override
  protected void visit(For node) {
    printindent();
    out.printf("for (%s = ", node.loopVar.id);
    node.init.accept(this);
    out.print(", ");
    node.end.accept(this);
    out.printf(", %d) {\n", node.increment);
    node.body.accept(this);
    printindent();
    out.println("}");
  }

  @Override
  public void visit(Function node) {
    indent = 0;
    if (node.isCallout) {
      out.printf("callout %s;\n", node.id);
    } else {
      out.printf("%s %s(", node.returnType.toString(), node.id);
      List<Var> params = node.getParams();
      if (params.size() != 0) {
        for (int i = 0; i < params.size() - 1; i++) {
          out.printf("%s %s, ", params.get(i).type.toString(), params.get(i).id);
        }
        out.printf("%s %s", params.get(params.size() - 1).type.toString(), params.get(params.size() - 1).id);
      }
      out.println(") {");
      node.body.accept(this);
      printindent();
      out.println('}');
      out.println();
    }
  }

  @Override
  protected void visit(If node) {
    printindent();
    out.print("if (");
    node.cond.accept(this);
    out.print(") {\n");
    node.trueBlock.accept(this);
    printindent();
    out.println("} else {");
    node.falseBlock.accept(this);
    printindent();
    out.println('}');
  }

  @Override
  protected void visit(Pass node) {
    printindent();
    out.print("// pass\n");
  }

  @Override
  public void visit(Program node) {
    if (printSymtab) {
      ArrayList<Var> globals = node.globals.asList();
      for (Var global : globals) {
        printindent();
        out.print("// ");
        printVar(global);
        out.printf("\tBSS offset = %d\n", global.getStackOffset());
      }
    }

    for (StatementNode decl : node.varDecls) {
      decl.accept(this);
    }

    for (FunctionNode func : node.functions) {
      func.accept(this);
    }

    if (node.main != null) {
      node.main.accept(this);
    }
  }

  @Override
  protected void visit(Return node) {
    printindent();
    out.print("return");
    if (node.value != null) {
      out.print(" ");
      node.value.accept(this);
    }
    out.print(";\n");
  }

  @Override
  protected void visit(StringLiteral node) {
    out.printf("%s", node.value);
  }

  @Override
  protected void visit(UnparsedIntLiteral node) {
    out.print(node.value);
  }

  @Override
  protected void visit(VarDecl node) {
    printindent();
    printVar(node.var);
    out.println();
  }

  @Override
  protected void visit(VarExpr node) {
    out.print(node.var.id);
  }

  @Override
  protected void visit(While node) {
    printindent();
    out.print("while (");
    node.cond.accept(this);
    out.println(") {");
    node.body.accept(this);
    printindent();
    out.println('}');
  }
}

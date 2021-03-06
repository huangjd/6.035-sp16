package edu.mit.compilers.common;

import java.io.PrintStream;
import java.util.ArrayList;

public class ErrorLogger {

  static public void reset(String fileName) {
    log = new ErrorLogger();
  }

  public ArrayList<String> errors = new ArrayList<>();

  static String a = ": error: ";
  static String b = ": warning: ";

  static void process(SourcePosition pos, Object... args) {
    String res = "";
    if (pos != null) {
      res = pos.toString();
    } else {
      res = "????:?:? (Unable to determine the loaction of source code)";
    }
    res += a;
    for (Object o : args) {
      if (o != null) {
        res += o.toString();
      } else {
        res += "!<null>";
      }
    }
    log.errors.add(res);
  }

  static public void logError(TypeException e) {
    switch (e.subtype) {
    case MISMATCH:
      if (e.expr != null) {
        if (e.expected != Type.NONE) {
          process(e.pos, "expecting type \'", e.expected, "\', got type \'", e.actual, "\'\n", e.expr);
        } else {
          process(e.pos, "function declared to return type void but got type \'", e.actual, "\'\n", e.expr);
        }
      } else if (e.var != null) {
        if (e.expected == Type.CALL) {
          process(e.pos, "\'", e.actual, "\' is not callable\n", e.var);
        } else {
          process(e.pos, "expecting type \'", e.expected, "\', got type \'", e.actual, "\'\n", e.var);
        }
      } else {
        process(e.pos, "expecting type \'", e.expected, "\', got type \'", e.actual, "\'");
      }
      return;
    case EXPECTED_PRIMITIVE:
      if (e.expr != null) {
        process(e.pos, "expecting primitive type, got type \'", e.actual, "\'\n", e.expr);
      } else if (e.var != null) {
        process(e.pos, "expecting primitive type, got type \'", e.actual, "\'\n", e.var);
      } else {
        process(e.pos, "expecting primitive type, got type \'", e.actual, "\'");
      }
      return;
    case EXPECTED_ARRAY:
      if (e.expr != null) {
        process(e.pos, "expecting array type, got type \'", e.actual, "\'\n", e.expr);
      } else if (e.var != null) {
        process(e.pos, "expecting array type, got type \'", e.actual, "\'\n", e.var);
      } else {
        process(e.pos, "expecting array type, got type \'", e.actual, "\'");
      }
      return;
    case EXPECTED_VARIABLE:
      if (e.expr != null) {
        process(e.pos, "expecting primitive or array type, got type \'", e.actual, "\'\n", e.expr);
      } else if (e.var != null) {
        process(e.pos, "expecting primitive or array type, got type \'", e.actual, "\'\n", e.var);
      }
      return;
    case CONFLICT:
      if (e.expr2 != null) {
        process(e.pos, "conflicting types between two sub-expressions: \n",
            "left sub-expression \"", e.expr, "\" is of type \'", e.expected, "\n",
            "right sub-expression \"", e.expr2, "\" is of type \'", e.actual);
      } else {
        process(e.pos, "expecting type \'", e.expected, "\' for parameter \"", e.var, "\", got \"", e.expr,
            "\" of type \'", e.actual, "\'");
      }
      return;
    }
  }

  static public void logError(UndeclaredSymbolException e) {
    if (e.symbolTable != null) {
      process(e.pos, "variable \"", e.symbol, "\" was not declared in this scope");
    } else {
      process(e.pos, "function \"", e.symbol, "\" was not declared");
    }
  }

  static public void logError(RedeclaredSymbolException e) {
    if (e.original != null) {
      if (e.f != null) {
        process(e.pos, "redeclaration of \'", e.f.id, "\' as a different type of symbol");
      } else {
        if (e.original.type == e.redeclared.type) {
          process(e.pos, "redeclaration of variable \'", e.original.type, " ", e.redeclared, "\'");
        } else {
          process(e.pos, "redeclaration of variable \'", e.original.type, "\' as a different type of symbol \'",
              e.redeclared.type, " ", e.redeclared, "\'");
        }
      }
    } else {
      process(e.pos, "redeclaration of function \"", e.f.id, "\"");
    }
  }

  static public void logError(ArgumentsException e) {
    if (e.expected > e.actual) {
      process(e.pos, "too few arguments to function \"", e.func.getSignature(), "\", expected ", e.expected, ", got ",
          e.actual);
    } else if (e.expected < e.actual) {
      process(e.pos, "too many arguments to function \"", e.func.getSignature(), "\", expected ", e.expected, ", got ",
          e.actual);
    }
  }

  static public void logError(GeneralException e) {
    process(e.pos, e.msg);
  }

  static public void logError(BoundsException e) {
    process(e.pos, "for loop increment must be a positive int_literal, got \"", e.value, "\"");
  }

  static public void logError(ArrayDeclSizeException e) {
    process(e.pos, "size of array \"", e.value, "[]\" is not a positive integer");
  }

  static public void logError(InvalidBreakStatementException e) {
    process(e.pos, e.isContinue ? "continue" : "break", " statement is not in a for statement or a while statement");
  }

  static public void logError(Exception e) {
    process(null, e);
  }

  static public void printErrors() {
    for (String s : log.errors) {
      System.err.println(s);
    }
  }

  static public void printErrors(PrintStream out) {
    for (String s : log.errors) {
      out.println(s);
    }
  }

  public static ErrorLogger log = new ErrorLogger();
}

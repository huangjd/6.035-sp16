package edu.mit.compilers.common;

import java.util.ArrayList;

public class ErrorLogger {

  static public void reset(String fileName) {
    log = new ErrorLogger();
  }

  ArrayList<String> errors = new ArrayList<>();

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
        process(e.pos, "expecting type \'", e.expected, "\', got type \'", e.actual, "\'\n", e.expr);
      } else if (e.var != null) {
        process(e.pos, "expecting type \'", e.expected, "\', got type \'", e.actual, "\'\n", e.var);
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
      if (e.original.type == e.redeclared.type) {
        process(e.pos, "redeclaration of variable \'", e.original.type, " ", e.redeclared, "\'");
      } else {
        process(e.pos, "redeclaration of variable \'", e.original.type, "\' as a different type of symbol \'",
            e.redeclared.type, " ", e.redeclared, "\'");
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

  static public void logError(BoundsException e) {
    process(e.pos, "for loop increment must be a positive int_literal, got \"", e.value, "\"");
  }

  static public void logError(ArrayDeclSizeException e) {
    process(e.pos, "size of array \"", e.value, "[]\" is not a positive integer");
  }

  static public void logError(Exception e) {
    process(null, e);
  }

  static public void printErrors() {
    for (String s : log.errors) {
      System.out.println(s);
    }
  }

  public static ErrorLogger log = new ErrorLogger();
}

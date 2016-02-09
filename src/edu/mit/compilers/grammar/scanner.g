header {
package edu.mit.compilers.grammar;
}

options
{
  mangleLiteralPrefix = "TK_";
  language = "Java";
}

{@SuppressWarnings("unchecked")}
class DecafScanner extends Lexer;
options
{
  k = 2;
}

tokens 
{
  "boolean";
  "break";
  "callout";
  "continue";
  "else";
  "false";
  "for";
  "while";
  "if";
  "int";
  "return";
  "true";
  "void";
}

// Selectively turns on debug tracing mode.
// You can insert arbitrary Java code into your parser/lexer this way.
{
  /** Whether to display debug information. */
  private boolean trace = false;

  public void setTrace(boolean shouldTrace) {
    trace = shouldTrace;
  }
  @Override
  public void traceIn(String rname) throws CharStreamException {
    if (trace) {
      super.traceIn(rname);
    }
  }
  @Override
  public void traceOut(String rname) throws CharStreamException {
    if (trace) {
      super.traceOut(rname);
    }
  }
}

LCURLY options { paraphrase = "{"; } : "{";
RCURLY options { paraphrase = "}"; } : "}";

LSQUARE options { paraphrase = "["; } : "[";
RSQUARE options { paraphrase = "]"; } : "]";

LPAREN options { paraphrase = "("; } : "(";
RPAREN options { paraphrase = ")"; } : ")";

SEMICOLON options { paraphrase = ";"; } : ';';

ID options { paraphrase = "an identifier"; } :
  ALPHA (ALNUM)*; 
  
CHARLITERAL : '\'' (ESC|~('\''|'\"'|'\\'|'\n'|'\t')) '\'';
INTLITERAL : ((NUMBER)+ | "0x" (NUMBER|'A'..'F'|'a'..'f')+);
STRINGLITERAL : '"' (ESC|~('\''|'\"'|'\\'|'\n'|'\t'))* '"';

COMMA options { paraphrase = ","; } : ',';
QUESTION : '?';
COLON options { paraphrase = ":"; } : ':';
AT_SIGN : '@';
MINUS : '-';
PLUS : '+';
EXCLAMATION : '!';
EQUAL : '=';
MUL_OP : '*' | '/' | '%';
REL_OP : '<' | '>' | "<=" | ">=";
EQ_OP : "==" | "!=";
AND : "&&";
OR : "||"; 

// Note that here, the {} syntax allows you to literally command the lexer
// to skip mark this token as skipped, or to advance to the next line
// by directly adding Java commands.
WS : (' '|'\t'|('\n' {newline();})) {_ttype = Token.SKIP; };
COMMENT : "//" (~'\n')* '\n' {_ttype = Token.SKIP; newline(); };

protected
ESC :  '\\' ('n'|'t'|'\\'|'\''|'\"');

protected
ALNUM : ALPHA | NUMBER;

protected
ALPHA : ('a'..'z')|('A'..'Z')|'_';

protected
NUMBER : ('0'..'9');

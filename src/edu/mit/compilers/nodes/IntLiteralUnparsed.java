package edu.mit.compilers.nodes;

import edu.mit.compilers.common.*;

public class IntLiteralUnparsed extends Literal {

  private String value;

  public IntLiteralUnparsed(String v, SourcePosition pos) {
    super(pos);
    try {
    	value = v;
      hashCache = v.hashCode();
		} catch (Exception e) {
			ErrorLogger.logError(ErrorLogger.ErrorMask.SEMANTICS, pos, this.toString(), ErrorType.TYPEERROR);
		} 
  }

  @Override
  public String toString() {
    return value;
  }

  @Override
  void dispatch(Visitor visitor) {
    visitor.visit(this);
  }

  public long getValue() {
    return Long.parseLong(value);
  }

  @Override
  public long getValueLong() {
    return Long.parseLong(value);
  }

  @Override
  public Type getType() {
    return Type.INT;
  }
}
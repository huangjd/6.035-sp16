package edu.mit.compilers.nodes;

import edu.mit.compilers.common.*;

public class IntLiteral extends Literal {

  private long value;

  public IntLiteral(long v, SourcePosition pos) {
    super(pos);
    try {
    	value = v;
      hashCache = Long.hashCode(v);
  	} catch (Exception e) {
  		ErrorLogger.logError(ErrorLogger.ErrorMask.SEMANTICS, pos, this.toString(), ErrorType.TYPEERROR);
  	} 
  }

  @Override
  public String toString() {
    return Long.toString(value);
  }

  @Override
  void dispatch(Visitor visitor) {
    visitor.visit(this);
  }

  public long getValue() {
    return value;
  }

  @Override
  public long getValueLong() {
    return value;
  }

  @Override
  public Type getType() {
    return Type.INT;
  }
}
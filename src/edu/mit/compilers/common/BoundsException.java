package edu.mit.compilers.common;

public class BoundsException extends IllegalArgumentException {

  private static final long serialVersionUID = 8613308941865819544L;

  public final String value;
  public final Long lower, upper;
  public final SourcePosition pos;

  public BoundsException(long value, Long lower, Long upper, SourcePosition pos) {
    this.value = Long.toString(value);
    this.lower = lower;
    this.upper = upper;
    this.pos = pos;
  }

  public BoundsException(String value, Long lower, Long upper, SourcePosition pos) {
    this.value = value;
    this.lower = lower;
    this.upper = upper;
    this.pos = pos;
  }

  public BoundsException(BoundsException e, SourcePosition pos) {
    this.value = e.value;
    this.lower = e.lower;
    this.upper = e.upper;
    this.pos = pos;
  }
}

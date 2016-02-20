package edu.mit.compilers.common;

public class SourcePosition {

  public final int lineNum, colNum;

  public SourcePosition() {
    lineNum = 0;
    colNum = 0;
  }

  public SourcePosition(int lineNum) {
    this.lineNum = lineNum;
    colNum = 0;
  }

  public SourcePosition(int lineNum, int colNum) {
    this.lineNum = lineNum;
    this.colNum = colNum;
  }
}

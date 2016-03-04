package edu.mit.compilers.common;

public class SourcePosition {

  public final int lineNum, colNum;
  public final String filename;

  public SourcePosition() {
    this("", 0, 0);
  }

  public SourcePosition(String fileName) {
    this(fileName, 0, 0);
  }

  public SourcePosition(String fileName, int lineNum) {
    this(fileName, lineNum, 0);
  }

  public SourcePosition(String fileName, int lineNum, int colNum) {
    this.filename = fileName;
    this.lineNum = lineNum;
    this.colNum = colNum;
  }

  @Override
  public String toString() {
    return filename + ":" + String.valueOf(lineNum) + ":" + String.valueOf(colNum);
  }
}

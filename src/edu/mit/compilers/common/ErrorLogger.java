package edu.mit.compilers.common;

public class ErrorLogger {

  public enum ErrorMask {
    TOKEN(1),
    PARSER(2),
    SEMANTICS(4),
    LINKER(8),
    WARNING1(16),
    WARNING2(32),
    WARNING3(64),
    DEBUG(128);

    private int _val;

    ErrorMask(int val) {
      _val = val;
    }

    public int getValue() {
      return _val;
    }

    public ErrorMask make(int mask) {
      // @ TODO
      return null;
    }
  }

  private String fileName;

  static public void reset(String fileName) {
    log = new ErrorLogger();
    log.fileName = fileName;
  }

  static public void logError(ErrorMask type, SourcePosition pos, String source, String message) {
    // TODO
  }

  static public void logError(ErrorMask type, SourcePosition pos, String source, ErrorType errno) {
    // TODO
  }

  static public void logError(ErrorMask type, SourcePosition pos, String source, String offendingToken,
      ErrorType errno) {
    // TODO
  }

  static public void printErrors(ErrorMask mask) {
    // TODO
  }

  private static ErrorLogger log = null;
}

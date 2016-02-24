package edu.mit.compilers.common;

public class MathUtil {
  public static int roundUp(int value, int alignment) {
    return (value + 7) / alignment * alignment;
  }

  public static long roundUp(long value, long alignment) {
    return (value + 7l) / alignment * alignment;
  }
}

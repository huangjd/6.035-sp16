package edu.mit.compilers.common;

public class MathUtil {
  public static int roundUp(int value, int alignment) {
    return (value + 7) / alignment * alignment;
  }

  public static long roundUp(long value, long alignment) {
    return (value + 7l) / alignment * alignment;
  }

  public static long parseInt(String value, Long lowerBound, Long upperBound) throws IllegalArgumentException {
    long v;
    if (value.length() > 2 && value.charAt(0) == '0' && value.charAt(1) == 'x') {
      v = Long.parseLong(value.substring(2), 16);
    } else {
      v = Long.parseLong(value);
    }
    if (lowerBound != null) {
      if (v < lowerBound) {
        throw new BoundsException(value, lowerBound, upperBound, null);
      }
    }
    if (upperBound != null) {
      if (v >= upperBound) {
        throw new BoundsException(value, lowerBound, upperBound, null);
      }
    }
    return v;
  }
}

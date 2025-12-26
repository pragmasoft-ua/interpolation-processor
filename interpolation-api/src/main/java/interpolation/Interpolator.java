package interpolation;

/**
 * Immutable record holding parsed template data. Instances are created at compile-time and cached
 * in bytecode.
 */
public record Interpolator(
  String[] fragments /** Template split by variables: ["Hello ", ", you have ", " items"] */,
  VarInfo[] varInfos /** Compile-time metadata records */
) {
  /**
   * Placeholder method - replaced by annotation processor. Never actually called at runtime.
   */
  public static String str(String template) {
    throw new UnsupportedOperationException(
        "Interpolator annotation processor is not properly installed!");
  }

  /**
   * Runtime interpolation method. Combines fragments with provided values.
   */
  public String process(Object... values) {
    final StringBuilder sb = new StringBuilder();
    for (int i = 0; i < fragments.length; i++) {
      sb.append(fragments[i]);
      if (i < values.length) {
        sb.append(values[i]);
      }
    }
    return sb.toString();
  }
}

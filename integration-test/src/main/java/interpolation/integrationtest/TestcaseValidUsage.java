package interpolation.integrationtest;

import static interpolation.Interpolator.str;

public class TestcaseValidUsage {

  public static String greet(String name, int count) {
    final var msg = str("Hello ${name}, you have ${count} items");
    return msg;
  }

}

package interpolation.integrationtest;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

public class IntegrationTest {

  @Test(expected = UnsupportedOperationException.class)
  public void testValidUsage() {

    String result = TestcaseValidUsage.greet("Alice", 5);
    MatcherAssert.assertThat(result, Matchers.equalTo("Hello Alice, you have 5 items"));

  }

}

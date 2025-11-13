package interpolation.processor;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Unit test for {@link InterpolationMethodProcessorMessages}.
 *
 * TODO: replace the example testcases with your own testcases
 *
 */
public class InterpolationMethodProcessorMessagesTest {

    @Test
    public void test_enum() {

        MatcherAssert.assertThat(InterpolationMethodProcessorCompilerMessages.ERROR_COULD_NOT_CREATE_CLASS.getCode(), Matchers.startsWith("InterpolationMethod"));
        MatcherAssert.assertThat(InterpolationMethodProcessorCompilerMessages.ERROR_COULD_NOT_CREATE_CLASS.getMessage(), Matchers.containsString("create class"));

    }


}

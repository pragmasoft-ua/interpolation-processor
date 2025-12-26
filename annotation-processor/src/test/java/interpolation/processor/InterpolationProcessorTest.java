package interpolation.processor;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;
import org.junit.Test;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;

/**
 * Tests of {@link InterpolationProcessor}.
 */

public class InterpolationProcessorTest {

  @Test
  public void test_valid_usage() {

    Compilation compilation = javac().withProcessors(new InterpolationProcessor())
        .compile(JavaFileObjects.forResource("TestcaseValidUsage.java"));

    assertThat(compilation).succeededWithoutWarnings();
  }

}

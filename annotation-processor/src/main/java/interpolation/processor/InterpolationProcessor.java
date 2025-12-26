package interpolation.processor;

import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.element.TypeElement;

/**
 * Interpolation annotation processor
 */

@SupportedAnnotationTypes("*")
@SupportedSourceVersion(javax.lang.model.SourceVersion.RELEASE_25)
public class InterpolationProcessor extends AbstractProcessor {

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    // Processing logic here
    return false;
  }
}

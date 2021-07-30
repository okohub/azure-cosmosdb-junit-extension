package okohub.azure.cosmosdb.junit;

import java.lang.reflect.AnnotatedElement;
import java.util.Objects;
import java.util.Optional;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * @author Onur Kagan Ozcan
 */
abstract class AbstractCosmosDataExtension implements CosmosDataExtension {

  @Override
  public void beforeEach(ExtensionContext context) throws Exception {
    Optional<CosmosData> annotationContainer = findAnnotation(context);
    if (annotationContainer.isEmpty()) {
      return;
    }
    doBeforeEach(context, annotationContainer.get());
  }

  abstract void doBeforeEach(ExtensionContext context, CosmosData annotation) throws Exception;

  @Override
  public void afterEach(ExtensionContext context) throws Exception {
    Optional<CosmosData> annotationContainer = findAnnotation(context);
    if (annotationContainer.isEmpty()) {
      return;
    }
    doAfterEach(context, annotationContainer.get());
  }

  abstract void doAfterEach(ExtensionContext context, CosmosData annotation) throws Exception;

  private Optional<CosmosData> findAnnotation(ExtensionContext context) {
    Optional<AnnotatedElement> elementContainer = context.getElement();
    if (elementContainer.isEmpty()) {
      return Optional.empty();
    }
    AnnotatedElement annotatedElement = elementContainer.get();
    CosmosData annotation = annotatedElement.getAnnotation(CosmosData.class);
    if (Objects.isNull(annotation)) {
      return Optional.empty();
    }
    return Optional.of(annotation);
  }
}

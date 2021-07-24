package com.okohub.azure.cosmosdb.junit;

import java.util.Optional;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterResolver;

/**
 * @author onurozcan
 */
public abstract class AbstractCosmosScriptExtension implements BeforeEachCallback,
                                                               AfterEachCallback,
                                                               ParameterResolver {

  @Override
  public void beforeEach(ExtensionContext context) throws Exception {
    Optional<CosmosScript> annotationContainer = getAnnotation(context);
    if (annotationContainer.isEmpty()) {
      return;
    }
    CosmosScript annotation = annotationContainer.get();
    doBeforeEach(context, annotation);
  }

  public abstract void doBeforeEach(ExtensionContext context, CosmosScript annotation) throws Exception;

  @Override
  public void afterEach(ExtensionContext context) throws Exception {
    Optional<CosmosScript> annotationContainer = getAnnotation(context);
    if (annotationContainer.isEmpty()) {
      return;
    }
    CosmosScript annotation = annotationContainer.get();
    doAfterEach(context, annotation);
  }

  public abstract void doAfterEach(ExtensionContext context, CosmosScript annotation) throws Exception;

  private Optional<CosmosScript> getAnnotation(ExtensionContext context) {
    return context.getTestMethod().map(m -> m.getAnnotation(CosmosScript.class));
  }
}

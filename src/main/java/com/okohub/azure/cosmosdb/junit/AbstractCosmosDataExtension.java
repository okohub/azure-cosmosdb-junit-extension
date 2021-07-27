package com.okohub.azure.cosmosdb.junit;

import java.util.Optional;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterResolver;

/**
 * @author onurozcan
 */
abstract class AbstractCosmosDataExtension implements BeforeEachCallback,
                                                      AfterEachCallback,
                                                      ParameterResolver {

  @Override
  public void beforeEach(ExtensionContext context) throws Exception {
    Optional<CosmosData> annotationContainer = getAnnotation(context);
    if (annotationContainer.isEmpty()) {
      return;
    }
    CosmosData annotation = annotationContainer.get();
    doBeforeEach(context, annotation);
  }

  abstract void doBeforeEach(ExtensionContext context, CosmosData annotation) throws Exception;

  @Override
  public void afterEach(ExtensionContext context) throws Exception {
    Optional<CosmosData> annotationContainer = getAnnotation(context);
    if (annotationContainer.isEmpty()) {
      return;
    }
    CosmosData annotation = annotationContainer.get();
    doAfterEach(context, annotation);
  }

  abstract void doAfterEach(ExtensionContext context, CosmosData annotation) throws Exception;

  private Optional<CosmosData> getAnnotation(ExtensionContext context) {
    return context.getTestMethod().map(m -> m.getAnnotation(CosmosData.class));
  }
}

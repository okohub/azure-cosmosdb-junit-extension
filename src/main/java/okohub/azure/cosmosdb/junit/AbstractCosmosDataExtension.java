package okohub.azure.cosmosdb.junit;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExtensionContext;

import static org.junit.jupiter.api.extension.ConditionEvaluationResult.disabled;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled;

/**
 * @author Onur Kagan Ozcan
 */
abstract class AbstractCosmosDataExtension implements CosmosDataExtension {

  @Override
  public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
    return context.getElement()
                  .filter(el -> el.isAnnotationPresent(CosmosData.class))
                  .map(el -> enabled("@CosmosData annotation found"))
                  .orElseGet(() -> disabled("@CosmosData annotation not found"));
  }

  @Override
  public void beforeEach(ExtensionContext context) throws Exception {
    CosmosData annotation = context.getElement().get().getAnnotation(CosmosData.class);
    doBeforeEach(context, annotation);
  }

  abstract void doBeforeEach(ExtensionContext context, CosmosData annotation) throws Exception;

  @Override
  public void afterEach(ExtensionContext context) throws Exception {
    CosmosData annotation = context.getElement().get().getAnnotation(CosmosData.class);
    doAfterEach(context, annotation);
  }

  abstract void doAfterEach(ExtensionContext context, CosmosData annotation) throws Exception;
}

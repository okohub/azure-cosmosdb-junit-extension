package com.okohub.azure.cosmosdb.junit;

import com.azure.cosmos.CosmosAsyncClient;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * @author onurozcan
 */
public class AsyncCosmosScriptExtension extends AbstractCosmosScriptExtension {

  private final CosmosAsyncClient cosmosClient;

  public AsyncCosmosScriptExtension(CosmosAsyncClient cosmosClient) {
    this.cosmosClient = cosmosClient;
  }

  @Override
  public void doBeforeEach(ExtensionContext context, CosmosScript annotation) throws Exception {
    ResourceOperator resourceOperator = new ResourceOperator(cosmosClient, annotation);
    resourceOperator.createDatabase();
    resourceOperator.createContainer();
    resourceOperator.populate(50);
  }

  @Override
  public void doAfterEach(ExtensionContext context, CosmosScript annotation) throws Exception {
    ResourceOperator resourceOperator = new ResourceOperator(cosmosClient, annotation);
    resourceOperator.deleteDatabase();
  }
}

package com.okohub.azure.cosmosdb.junit;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;

/**
 * @author onurozcan
 */
public class AsyncClientCosmosDataExtension extends AbstractCosmosDataExtension {

  private final CosmosAsyncClient cosmosClient;

  public AsyncClientCosmosDataExtension(CosmosAsyncClient client) {
    this.cosmosClient = client;
  }

  public AsyncClientCosmosDataExtension(String endpoint, String key) {
    this.cosmosClient = new CosmosClientBuilder().gatewayMode()
                                                 .endpointDiscoveryEnabled(false)
                                                 .endpoint(endpoint)
                                                 .key(key)
                                                 .buildAsyncClient();
  }

  @Override
  public void doBeforeEach(ExtensionContext context, CosmosData annotation) throws Exception {
    ResourceOperator resourceOperator = new ResourceOperator(cosmosClient, annotation);
    resourceOperator.createDatabase();
    resourceOperator.createContainer();
    resourceOperator.populate();
  }

  @Override
  public void doAfterEach(ExtensionContext context, CosmosData annotation) {
    ResourceOperator resourceOperator = new ResourceOperator(cosmosClient, annotation);
    resourceOperator.deleteDatabase();
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    return parameterContext.getParameter().getType() == CosmosAsyncClient.class;
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    return cosmosClient;
  }
}

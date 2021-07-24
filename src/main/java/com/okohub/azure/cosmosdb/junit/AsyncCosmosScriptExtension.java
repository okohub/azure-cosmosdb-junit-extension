package com.okohub.azure.cosmosdb.junit;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.GatewayConnectionConfig;
import java.lang.reflect.Field;
import java.time.Duration;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;

/**
 * @author onurozcan
 */
public class AsyncCosmosScriptExtension extends AbstractCosmosScriptExtension {

  private final CosmosAsyncClient cosmosClient;

  public AsyncCosmosScriptExtension(String endpoint, String key) {
    GatewayConnectionConfig config = hackedGatewayConfig();
    this.cosmosClient = new CosmosClientBuilder().gatewayMode(config)
                                                 .endpointDiscoveryEnabled(false)
                                                 .endpoint(endpoint)
                                                 .key(key)
                                                 .buildAsyncClient();
  }

  /**
   * for large documents, 5 seconds sometimes not enough.
   * However, requestTimeout setter in config is hidden for an unknown reason (still looking for why)
   * So a bit hacky thing to make it double
   *
   * @link {https://github.com/Azure/azure-sdk-for-java/pull/11702}
   * @return customized GatewayConnectionConfig
   */
  private GatewayConnectionConfig hackedGatewayConfig() {
    GatewayConnectionConfig config = new GatewayConnectionConfig();
    try {
      Field requestTimeout = GatewayConnectionConfig.class.getDeclaredField("requestTimeout");
      requestTimeout.setAccessible(true);
      requestTimeout.set(config, Duration.ofSeconds(10));
    } catch (Exception ex) {
      //silent
    }
    return config;
  }

  @Override
  public void doBeforeEach(ExtensionContext context, CosmosScript annotation) throws Exception {
    ResourceOperator resourceOperator = new ResourceOperator(cosmosClient, annotation);
    resourceOperator.createDatabase();
    resourceOperator.createContainer();
    resourceOperator.populate();
  }

  @Override
  public void doAfterEach(ExtensionContext context, CosmosScript annotation) {
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

package okohub.azure.cosmosdb.junit;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;

/**
 * @author Onur Kagan Ozcan
 */
final class AsyncClientCosmosDataExtension extends AbstractCosmosDataExtension {

  private final CosmosAsyncClient cosmosClient;

  AsyncClientCosmosDataExtension(String endpoint, String key) {
    this.cosmosClient = new CosmosClientBuilder().gatewayMode()
                                                 .endpointDiscoveryEnabled(false)
                                                 .endpoint(endpoint)
                                                 .key(key)
                                                 .buildAsyncClient();
  }

  @Override
  public void doBeforeEach(ExtensionContext context, CosmosData annotation) throws Exception {
    ResourceOperator operator = new AsyncResourceOperator(cosmosClient,
                                                          annotation,
                                                          new ResourceReader(),
                                                          findPopulator(annotation));
    operator.createDatabase();
    operator.createContainer();
    operator.populate();
  }

  @Override
  public void doAfterEach(ExtensionContext context, CosmosData annotation) {
    ResourceOperator operator = new AsyncResourceOperator(cosmosClient,
                                                          annotation,
                                                          new ResourceReader(),
                                                          findPopulator(annotation));
    operator.deleteDatabase();
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

  private AsyncCosmosDBPopulator findPopulator(CosmosData annotation) {
    return annotation.useBulk()
        ? new AsyncCosmosDBBulkPopulator(annotation)
        : new AsyncCosmosDBSinglePopulator(annotation);
  }
}

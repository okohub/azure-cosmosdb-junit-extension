package okohub.azure.cosmosdb.junit;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;

/**
 * @author Onur Kagan Ozcan
 */
final class SyncClientCosmosDataExtension extends AbstractCosmosDataExtension {

  private final CosmosClient cosmosClient;

  SyncClientCosmosDataExtension(String endpoint, String key) {
    this.cosmosClient = new CosmosClientBuilder().gatewayMode()
                                                 .endpointDiscoveryEnabled(false)
                                                 .endpoint(endpoint)
                                                 .key(key)
                                                 .buildClient();
  }

  @Override
  public void doBeforeEach(ExtensionContext context, CosmosData annotation) throws Exception {
    ResourceOperator operator = new SyncResourceOperator(cosmosClient,
                                                         annotation,
                                                         new ResourceReader(),
                                                         findPopulator(annotation));
    operator.createDatabase();
    operator.createContainer();
    operator.populate();
  }

  @Override
  public void doAfterEach(ExtensionContext context, CosmosData annotation) {
    ResourceOperator operator = new SyncResourceOperator(cosmosClient,
                                                         annotation,
                                                         new ResourceReader(),
                                                         findPopulator(annotation));
    operator.deleteDatabase();
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    return parameterContext.getParameter().getType() == CosmosClient.class;
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    return cosmosClient;
  }

  private SyncCosmosDBPopulator findPopulator(CosmosData annotation) {
    return annotation.useBulk()
        ? new SyncCosmosDBBulkPopulator(annotation)
        : new SyncCosmosDBSinglePopulator(annotation);
  }
}

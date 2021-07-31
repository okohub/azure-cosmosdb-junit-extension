package okohub.azure.cosmosdb.junit.sync;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import okohub.azure.cosmosdb.junit.CosmosData;
import okohub.azure.cosmosdb.junit.core.AbstractCosmosDataExtension;
import okohub.azure.cosmosdb.junit.core.ResourceOperator;
import okohub.azure.cosmosdb.junit.core.ResourceReader;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;

/**
 * @author Onur Kagan Ozcan
 */
public final class SyncClientCosmosDataExtension extends AbstractCosmosDataExtension {

  private final CosmosClient cosmosClient;

  public SyncClientCosmosDataExtension(String endpoint, String key) {
    this.cosmosClient = new CosmosClientBuilder().gatewayMode()
                                                 .endpointDiscoveryEnabled(false)
                                                 .endpoint(endpoint)
                                                 .key(key)
                                                 .buildClient();
  }

  @Override
  protected void doBeforeEach(ExtensionContext context, CosmosData annotation) throws Exception {
    ResourceOperator operator = new SyncResourceOperator(cosmosClient,
                                                         annotation,
                                                         new ResourceReader(),
                                                         findPopulator(annotation));
    operator.createDatabase();
    operator.createContainer();
    operator.populate();
  }

  @Override
  protected void doAfterEach(ExtensionContext context, CosmosData annotation) {
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

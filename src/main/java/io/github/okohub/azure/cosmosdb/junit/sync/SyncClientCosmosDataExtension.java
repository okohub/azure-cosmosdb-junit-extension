package io.github.okohub.azure.cosmosdb.junit.sync;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import io.github.okohub.azure.cosmosdb.junit.core.AbstractCosmosDataExtension;
import io.github.okohub.azure.cosmosdb.junit.core.Lazy;
import io.github.okohub.azure.cosmosdb.junit.core.ResourceReader;
import java.util.function.Supplier;
import io.github.okohub.azure.cosmosdb.junit.CosmosData;
import io.github.okohub.azure.cosmosdb.junit.core.ResourceOperator;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.TestInstancePreDestroyCallback;

/**
 * @author Onur Kagan Ozcan
 */
public final class SyncClientCosmosDataExtension extends AbstractCosmosDataExtension {

  private final Supplier<CosmosClient> cosmosClientSupplier;

  public SyncClientCosmosDataExtension(String endpoint, String key) {
    this(new Lazy<>(() -> new CosmosClientBuilder().gatewayMode()
                                                   .endpointDiscoveryEnabled(false)
                                                   .endpoint(endpoint)
                                                   .key(key)
                                                   .buildClient()));
  }

  public SyncClientCosmosDataExtension(Supplier<CosmosClient> cosmosClientSupplier) {
    this.cosmosClientSupplier = new Lazy<>(cosmosClientSupplier);
  }

  @Override
  protected void doBeforeEach(ExtensionContext context, CosmosData annotation) throws Exception {
    ResourceOperator operator = new SyncResourceOperator(cosmosClientSupplier.get(),
                                                         annotation,
                                                         new ResourceReader(),
                                                         findPopulator(annotation));
    operator.createDatabase();
    operator.createContainer();
    operator.populate();
  }

  @Override
  protected void doAfterEach(ExtensionContext context, CosmosData annotation) {
    ResourceOperator operator = new SyncResourceOperator(cosmosClientSupplier.get(),
                                                         annotation,
                                                         new ResourceReader(),
                                                         findPopulator(annotation));
    operator.deleteDatabase();
  }

  private SyncCosmosDBPopulator findPopulator(CosmosData annotation) {
    return annotation.useBulk()
        ? new SyncCosmosDBBulkPopulator(annotation)
        : new SyncCosmosDBSinglePopulator(annotation);
  }

  @Override
  public void preDestroyTestInstance(ExtensionContext context) {
    TestInstancePreDestroyCallback.preDestroyTestInstances(context, o -> cosmosClientSupplier.get().close());
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    return parameterContext.getParameter().getType() == CosmosClient.class;
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    return cosmosClientSupplier.get();
  }
}

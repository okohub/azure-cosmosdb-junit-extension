package io.github.okohub.azure.cosmosdb.junit.async;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
import io.github.okohub.azure.cosmosdb.junit.core.ResourceReader;
import java.util.function.Supplier;
import io.github.okohub.azure.cosmosdb.junit.CosmosData;
import io.github.okohub.azure.cosmosdb.junit.core.AbstractCosmosDataExtension;
import io.github.okohub.azure.cosmosdb.junit.core.Lazy;
import io.github.okohub.azure.cosmosdb.junit.core.ResourceOperator;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.TestInstancePreDestroyCallback;

/**
 * @author Onur Kagan Ozcan
 */
public final class AsyncClientCosmosDataExtension extends AbstractCosmosDataExtension {

  private final Supplier<CosmosAsyncClient> cosmosClientSupplier;

  public AsyncClientCosmosDataExtension(String endpoint, String key) {
    this(new Lazy<>(() -> new CosmosClientBuilder().gatewayMode()
                                                   .endpointDiscoveryEnabled(false)
                                                   .endpoint(endpoint)
                                                   .key(key)
                                                   .buildAsyncClient()));
  }

  public AsyncClientCosmosDataExtension(Supplier<CosmosAsyncClient> cosmosClientSupplier) {
    this.cosmosClientSupplier = new Lazy<>(cosmosClientSupplier);
  }

  @Override
  protected void doBeforeEach(ExtensionContext context, CosmosData annotation) throws Exception {
    ResourceOperator operator = new AsyncResourceOperator(cosmosClientSupplier.get(),
                                                          annotation,
                                                          new ResourceReader(),
                                                          findPopulator(annotation));
    operator.createDatabase();
    operator.createContainer();
    operator.populate();
  }

  @Override
  protected void doAfterEach(ExtensionContext context, CosmosData annotation) {
    ResourceOperator operator = new AsyncResourceOperator(cosmosClientSupplier.get(),
                                                          annotation,
                                                          new ResourceReader(),
                                                          findPopulator(annotation));
    operator.deleteDatabase();
  }

  private AsyncCosmosDBPopulator findPopulator(CosmosData annotation) {
    return annotation.useBulk()
        ? new AsyncCosmosDBBulkPopulator(annotation)
        : new AsyncCosmosDBSinglePopulator(annotation);
  }

  @Override
  public void preDestroyTestInstance(ExtensionContext context) {
    TestInstancePreDestroyCallback.preDestroyTestInstances(context, o -> cosmosClientSupplier.get().close());
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    return parameterContext.getParameter().getType() == CosmosAsyncClient.class;
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    return cosmosClientSupplier.get();
  }
}

package okohub.azure.cosmosdb.junit;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClient;
import java.util.function.Supplier;
import okohub.azure.cosmosdb.junit.async.AsyncClientCosmosDataExtension;
import okohub.azure.cosmosdb.junit.sync.SyncClientCosmosDataExtension;

/**
 * @author Onur Kagan Ozcan
 */
public final class CosmosDataExtensions {

  public static CosmosDataExtension withSyncClient(String endpoint, String key) {
    return new SyncClientCosmosDataExtension(endpoint, key);
  }

  public static CosmosDataExtension withSyncClient(Supplier<CosmosClient> clientSupplier) {
    return new SyncClientCosmosDataExtension(clientSupplier);
  }

  public static CosmosDataExtension withAsyncClient(String endpoint, String key) {
    return new AsyncClientCosmosDataExtension(endpoint, key);
  }

  public static CosmosDataExtension withAsyncClient(Supplier<CosmosAsyncClient> clientSupplier) {
    return new AsyncClientCosmosDataExtension(clientSupplier);
  }
}

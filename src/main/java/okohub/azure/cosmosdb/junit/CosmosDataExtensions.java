package okohub.azure.cosmosdb.junit;

/**
 * @author Onur Kagan Ozcan
 */
public final class CosmosDataExtensions {

  public static CosmosDataExtension withSyncClient(String endpoint, String key) {
    return new SyncClientCosmosDataExtension(endpoint, key);
  }

  public static CosmosDataExtension withAsyncClient(String endpoint, String key) {
    return new AsyncClientCosmosDataExtension(endpoint, key);
  }
}

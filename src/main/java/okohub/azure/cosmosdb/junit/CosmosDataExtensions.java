package okohub.azure.cosmosdb.junit;

/**
 * @author Onur Kagan Ozcan
 */
public final class CosmosDataExtensions {

  public static CosmosDataExtension sync(String endpoint, String key) {
    return new SyncClientCosmosDataExtension(endpoint, key);
  }

  public static CosmosDataExtension async(String endpoint, String key) {
    return new AsyncClientCosmosDataExtension(endpoint, key);
  }
}

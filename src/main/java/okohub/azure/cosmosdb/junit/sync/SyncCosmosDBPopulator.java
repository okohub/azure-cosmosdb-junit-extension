package okohub.azure.cosmosdb.junit.sync;

import com.azure.cosmos.CosmosContainer;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.stream.Stream;

/**
 * @author Onur Kagan Ozcan
 */
interface SyncCosmosDBPopulator {

  /**
   * @return request charge as request units (RU) consumed by the operation.
   */
  Double populate(CosmosContainer container, Stream<JsonNode> targetStream);
}

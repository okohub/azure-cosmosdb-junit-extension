package okohub.azure.cosmosdb.junit;

import com.azure.cosmos.CosmosContainer;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.stream.Stream;

/**
 * @author Onur Kagan Ozcan
 */
interface SyncCosmosDbPopulator {

  /**
   * @return request charge as request units (RU) consumed by the operation.
   */
  Double populate(CosmosContainer container, Stream<JsonNode> targetStream);
}

package okohub.azure.cosmosdb.junit;

import com.azure.cosmos.CosmosAsyncContainer;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.stream.Stream;

/**
 * @author Onur Kagan Ozcan
 */
interface AsyncCosmosDBPopulator {

  /**
   * @return request charge as request units (RU) consumed by the operation.
   */
  Double populate(CosmosAsyncContainer container, Stream<JsonNode> targetStream);
}

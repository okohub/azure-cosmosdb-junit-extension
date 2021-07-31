package okohub.azure.cosmosdb.junit.sync;

import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.stream.Stream;
import okohub.azure.cosmosdb.junit.CosmosData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Onur Kagan Ozcan
 */
final class SyncCosmosDBSinglePopulator implements SyncCosmosDBPopulator {

  private static final Logger LOGGER = LoggerFactory.getLogger(SyncCosmosDBSinglePopulator.class);

  private final CosmosData annotation;

  SyncCosmosDBSinglePopulator(CosmosData annotation) {
    this.annotation = annotation;
  }

  @Override
  public Double populate(CosmosContainer container, Stream<JsonNode> targetStream) {
    return targetStream.map(jn -> doSingleItemOperation(container, jn))
                       .map(response -> {
                         LOGGER.info("Finished single item. Status: {}, Millis: {}",
                                     response.getStatusCode(), response.getDuration().toMillis());
                         return response.getRequestCharge();
                       })
                       .reduce(0.0, Double::sum);
  }

  private CosmosItemResponse<JsonNode> doSingleItemOperation(CosmosContainer container,
                                                             JsonNode jn) {
    String key = jn.findPath(annotation.partitionKey()).textValue();
    PartitionKey partitionKey = new PartitionKey(key);
    return switch (annotation.operationType()) {
      case REPLACE -> container.replaceItem(jn, jn.findPath(annotation.idKey()).textValue(), partitionKey,
                                            new CosmosItemRequestOptions());
      case UPSERT -> container.upsertItem(jn, partitionKey, null);
      default -> container.createItem(jn, partitionKey, null);
    };
  }
}

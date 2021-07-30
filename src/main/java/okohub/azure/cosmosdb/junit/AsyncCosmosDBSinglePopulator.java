package okohub.azure.cosmosdb.junit;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Onur Kagan Ozcan
 */
final class AsyncCosmosDBSinglePopulator implements AsyncCosmosDBPopulator {

  private static final Logger LOGGER = LoggerFactory.getLogger(AsyncCosmosDBSinglePopulator.class);

  private final CosmosData annotation;

  AsyncCosmosDBSinglePopulator(CosmosData annotation) {
    this.annotation = annotation;
  }

  @Override
  public Double populate(CosmosAsyncContainer container, Stream<JsonNode> targetStream) {
    return Flux.fromStream(targetStream)
               .flatMap(jn -> newSingleItemOperation(container, jn))
               .flatMap(response -> {
                 LOGGER.info("Finished single item. Status: {}, Millis: {}",
                             response.getStatusCode(), response.getDuration().toMillis());
                 return Mono.just(response.getRequestCharge());
               })
               .reduce(Double::sum)
               .block();
  }

  private Mono<CosmosItemResponse<JsonNode>> newSingleItemOperation(CosmosAsyncContainer container,
                                                                    JsonNode jn) {
    String key = jn.findPath(annotation.partitionKey()).textValue();
    PartitionKey partitionKey = new PartitionKey(key);
    return switch (annotation.operationType()) {
      case REPLACE -> container.replaceItem(jn, jn.findPath(annotation.idKey()).textValue(), partitionKey);
      case UPSERT -> container.upsertItem(jn, partitionKey, null);
      default -> container.createItem(jn, partitionKey, null);
    };
  }
}

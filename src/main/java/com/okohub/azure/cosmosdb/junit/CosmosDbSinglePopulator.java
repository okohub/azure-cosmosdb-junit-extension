package com.okohub.azure.cosmosdb.junit;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author onurozcan
 */
final class CosmosDbSinglePopulator implements CosmosDbPopulator {

  private static final Logger LOGGER = LoggerFactory.getLogger(CosmosDbSinglePopulator.class);

  private final CosmosData annotation;

  CosmosDbSinglePopulator(CosmosData annotation) {
    this.annotation = annotation;
  }

  @Override
  public Double populate(CosmosAsyncContainer container, Stream<JsonNode> targetStream) {
    return Flux.fromStream(targetStream)
               .flatMap(jn -> {
                 String key = jn.findPath(annotation.partitionKey()).textValue();
                 PartitionKey partitionKey = new PartitionKey(key);
                 return container.createItem(jn, partitionKey, null);
               })
               .flatMap(response -> {
                 LOGGER.info("Finished single item. Status: {}, Millis: {}",
                             response.getStatusCode(), response.getDuration().toMillis());
                 return Mono.just(response.getRequestCharge());
               })
               .reduce(Double::sum)
               .block();
  }
}

package io.github.okohub.azure.cosmosdb.junit.async;

import com.azure.core.util.Context;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.implementation.batch.BatchRequestResponseConstants;
import com.azure.cosmos.models.CosmosBulkItemResponse;
import com.azure.cosmos.models.CosmosBulkOperationResponse;
import com.azure.cosmos.models.CosmosItemOperation;
import com.azure.cosmos.models.CosmosItemOperationType;
import com.fasterxml.jackson.databind.JsonNode;
import io.github.okohub.azure.cosmosdb.junit.core.BulkItemOperationCreator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import io.github.okohub.azure.cosmosdb.junit.CosmosData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Onur Kagan Ozcan
 */
final class AsyncCosmosDBBulkPopulator implements AsyncCosmosDBPopulator {

  private static final Logger LOGGER = LoggerFactory.getLogger(AsyncCosmosDBBulkPopulator.class);

  private final CosmosData annotation;

  AsyncCosmosDBBulkPopulator(CosmosData annotation) {
    this.annotation = annotation;
  }

  @Override
  public Double populate(CosmosAsyncContainer container, Stream<JsonNode> targetStream) {
    return Flux.fromStream(targetStream)
               .buffer(Math.min(annotation.bulkChunkSize(),
                                BatchRequestResponseConstants.MAX_OPERATIONS_IN_DIRECT_MODE_BATCH_REQUEST))
               .flatMap(jsonNodes -> newBulkOperation(container, jsonNodes, annotation.operationType()))
               .flatMap(response -> {
                 if (Objects.nonNull(response.getException())) {
                   LOGGER.error("Problem on single chunk.", response.getException());
                   return Mono.just(0.0);
                 }
                 CosmosBulkItemResponse itemResponse = response.getResponse();
                 LOGGER.info("Finished single chunk. Status: {}, Millis: {}",
                             itemResponse.getStatusCode(),
                             itemResponse.getDuration().toMillis());
                 return Mono.just(itemResponse.getRequestCharge());
               })
               .reduce(Double::sum)
               .block();
  }

  private Flux<CosmosBulkOperationResponse<Context>> newBulkOperation(CosmosAsyncContainer container,
                                                                      List<JsonNode> jsonNodes,
                                                                      CosmosItemOperationType opType) {
    LOGGER.info("Creating new chunk for bulk operation. Size: {}", jsonNodes.size());
    var operations = jsonNodes.stream()
                              .map(jn -> BulkItemOperationCreator.newOperation(annotation, jn, opType))
                              .toArray(CosmosItemOperation[]::new);
    var operationsFlux = Flux.fromArray(operations);
    return container.executeBulkOperations(operationsFlux);
  }
}

package okohub.azure.cosmosdb.junit.async;

import com.azure.core.util.Context;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosBulkItemResponse;
import com.azure.cosmos.CosmosBulkOperationResponse;
import com.azure.cosmos.CosmosItemOperation;
import com.azure.cosmos.CosmosItemOperationType;
import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import okohub.azure.cosmosdb.junit.CosmosData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.azure.cosmos.BulkOperations.getCreateItemOperation;
import static com.azure.cosmos.BulkOperations.getReplaceItemOperation;
import static com.azure.cosmos.BulkOperations.getUpsertItemOperation;
import static com.azure.cosmos.implementation.batch.BatchRequestResponseConstants.MAX_OPERATIONS_IN_DIRECT_MODE_BATCH_REQUEST;

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
               .buffer(Math.min(annotation.bulkChunkSize(), MAX_OPERATIONS_IN_DIRECT_MODE_BATCH_REQUEST))
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
    return container.processBulkOperations(Flux.fromArray(jsonNodes.stream()
                                                                   .map(jn -> newBulkItemOperation(jn, opType))
                                                                   .toArray(CosmosItemOperation[]::new)),
                                           null);
  }

  private CosmosItemOperation newBulkItemOperation(JsonNode jn, CosmosItemOperationType opType) {
    String key = jn.findPath(annotation.partitionKey()).textValue();
    PartitionKey partitionKey = new PartitionKey(key);
    return switch (opType) {
      case REPLACE -> getReplaceItemOperation(jn.findPath(annotation.idKey()).textValue(), jn, partitionKey);
      case UPSERT -> getUpsertItemOperation(jn, partitionKey);
      default -> getCreateItemOperation(jn, partitionKey);
    };
  }
}

package com.okohub.azure.cosmosdb.junit;

import com.azure.core.util.Context;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosBulkItemResponse;
import com.azure.cosmos.CosmosBulkOperationResponse;
import com.azure.cosmos.CosmosItemOperation;
import com.azure.cosmos.CosmosItemOperationType;
import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.azure.cosmos.BulkOperations.getCreateItemOperation;
import static com.azure.cosmos.BulkOperations.getReplaceItemOperation;
import static com.azure.cosmos.BulkOperations.getUpsertItemOperation;
import static com.azure.cosmos.implementation.batch.BatchRequestResponseConstants.MAX_OPERATIONS_IN_DIRECT_MODE_BATCH_REQUEST;

/**
 * @author onurozcan
 */
final class ResourceOperator {

  private static final Logger LOGGER = LoggerFactory.getLogger(ResourceOperator.class);

  private final ResourceReader resourceReader;

  private final CosmosAsyncClient cosmosClient;

  private final CosmosData annotation;

  ResourceOperator(CosmosAsyncClient cosmosClient, CosmosData annotation) {
    this.resourceReader = new ResourceReader();
    this.cosmosClient = cosmosClient;
    this.annotation = annotation;
  }

  void createDatabase() {
    String database = annotation.database();
    cosmosClient.createDatabaseIfNotExists(database).block();
  }

  void createContainer() {
    String database = annotation.database();
    String container = annotation.container();
    cosmosClient.getDatabase(database)
                .createContainerIfNotExists(container, "/" + annotation.partitionKey())
                .block();
  }

  void deleteDatabase() {
    String database = annotation.database();
    cosmosClient.getDatabase(database).delete().block();
  }

  void populate() throws Exception {
    String dataPath = annotation.path();
    Optional<String> dataContainer = resourceReader.readResource(dataPath);
    if (dataContainer.isEmpty()) {
      LOGGER.error("Can not populate because data is not found. DataPath: {}", dataPath);
      return;
    }
    CosmosAsyncContainer container = cosmosClient.getDatabase(annotation.database())
                                                 .getContainer(annotation.container());
    Stream<JsonNode> targetStream = resourceReader.readResourceContentAsJsonStream(dataContainer.get());
    //
    Double totalRequestCharge;
    if (annotation.useBulk()) {
      totalRequestCharge = doPopulateBulk(container, targetStream);
    } else {
      totalRequestCharge = doPopulateSimple(container, targetStream);
    }
    LOGGER.info("Finished data population. Total request charge in Azure Terms: {}", totalRequestCharge);
  }

  private Double doPopulateBulk(CosmosAsyncContainer container, Stream<JsonNode> targetStream) {
    return Flux.fromStream(targetStream)
               .buffer(Math.min(annotation.bulkChunkSize(), MAX_OPERATIONS_IN_DIRECT_MODE_BATCH_REQUEST))
               .flatMap(jsonNodes -> newBulkOperation(container, jsonNodes, annotation.bulkOperationType()))
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

  private Double doPopulateSimple(CosmosAsyncContainer container, Stream<JsonNode> targetStream) {
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

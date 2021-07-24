package com.okohub.azure.cosmosdb.junit;

import com.azure.core.util.Context;
import com.azure.cosmos.BulkProcessingOptions;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosBulkItemResponse;
import com.azure.cosmos.CosmosBulkOperationResponse;
import com.azure.cosmos.CosmosItemOperation;
import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.azure.cosmos.BulkOperations.getCreateItemOperation;

/**
 * @author onurozcan
 */
public class ResourceOperator {

  private static final Logger LOGGER = LoggerFactory.getLogger(ResourceOperator.class);

  private final ResourceReader resourceReader;

  private final CosmosAsyncClient cosmosClient;

  private final CosmosScript annotation;

  public ResourceOperator(CosmosAsyncClient cosmosClient, CosmosScript annotation) {
    this.resourceReader = new ResourceReader();
    this.cosmosClient = cosmosClient;
    this.annotation = annotation;
  }

  public void createDatabase() {
    String database = annotation.database();
    cosmosClient.createDatabaseIfNotExists(database).block();
  }

  public void createContainer() {
    String database = annotation.database();
    String container = annotation.container();
    cosmosClient.getDatabase(database)
                .createContainerIfNotExists(container,
                                            "/" + annotation.partitionKey())
                .block();
  }

  public void deleteDatabase() {
    String database = annotation.database();
    cosmosClient.getDatabase(database).delete().block();
  }

  public void populate() throws Exception {
    Optional<String> scriptContentContainer = resourceReader.readResource(annotation.script());
    if (scriptContentContainer.isEmpty()) {
      return;
    }
    //
    CosmosAsyncContainer container = cosmosClient.getDatabase(annotation.database())
                                                 .getContainer(annotation.container());
    //
    Stream<JsonNode> targetStream = resourceReader.readResourceContentAsJsonStream(scriptContentContainer.get());
    //
    Double totalRequestCharge = Flux.fromStream(targetStream)
                                    .buffer(annotation.chunkSize())
                                    .flatMap(jsonNodes -> newBulkOperation(container, jsonNodes))
                                    .flatMap(response -> {
                                      CosmosBulkItemResponse itemResponse = response.getResponse();
                                      LOGGER.debug("Finished single chunk. Status: {}, Millis: {}",
                                                   itemResponse.getStatusCode(),
                                                   itemResponse.getDuration().toMillis());
                                      return Mono.just(itemResponse.getRequestCharge());
                                    })
                                    .reduce(Double::sum)
                                    .block();
    LOGGER.info("Finished script load. Total request charge in Azure Terms: {}", totalRequestCharge);
  }

  private Flux<CosmosBulkOperationResponse<Context>> newBulkOperation(CosmosAsyncContainer container,
                                                                      List<JsonNode> jsonNodes) {
    var operationArray = jsonNodes.stream()
                                  .map(this::newBulkItemOperation)
                                  .toArray(CosmosItemOperation[]::new);
    return container.processBulkOperations(Flux.fromArray(operationArray), new BulkProcessingOptions<>(Context.NONE));
  }

  private CosmosItemOperation newBulkItemOperation(JsonNode jn) {
    String key = jn.findPath(annotation.partitionKey()).textValue();
    PartitionKey partitionKey = new PartitionKey(key);
    return getCreateItemOperation(jn, partitionKey);
  }
}

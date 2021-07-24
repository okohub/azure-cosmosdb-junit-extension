package com.okohub.azure.cosmosdb.junit;

import com.azure.cosmos.BulkProcessingOptions;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosItemOperation;
import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import reactor.core.publisher.Flux;

import static com.azure.cosmos.BulkOperations.getCreateItemOperation;
import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.joining;

/**
 * @author onurozcan
 */
public class ResourceOperator {

  private final static ObjectMapper MAPPER = new ObjectMapper();

  private final CosmosAsyncClient cosmosClient;

  private final CosmosScript annotation;

  public ResourceOperator(CosmosAsyncClient cosmosClient, CosmosScript annotation) {
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

  public void populate(Integer itemCount) throws Exception {
    Optional<String> scriptContentContainer = readScript(annotation.script());
    if (scriptContentContainer.isEmpty()) {
      return;
    }
    String scriptContent = scriptContentContainer.get();
    JsonNode jsonNode = MAPPER.readTree(scriptContent);
    Iterable<JsonNode> iterable = jsonNode::elements;
    Stream<JsonNode> targetStream = StreamSupport.stream(iterable.spliterator(), false);
    Stream<CosmosItemOperation> operationStream = targetStream.limit(itemCount)
                                                              .map(node -> {
                                                                String key = node.findPath(annotation.partitionKey())
                                                                                 .textValue();
                                                                PartitionKey partitionKey = new PartitionKey(key);
                                                                return getCreateItemOperation(node, partitionKey);
                                                              });
    BulkProcessingOptions bulkOptions = new BulkProcessingOptions();
    bulkOptions.setMaxMicroBatchSize(itemCount + 1);
    cosmosClient.getDatabase(annotation.database())
                .getContainer(annotation.container())
                .processBulkOperations(Flux.fromStream(operationStream), bulkOptions)
                .blockLast();
  }

  private Optional<String> readScript(String resourcePath) {
    InputStream resource = getClass().getResourceAsStream(resourcePath);
    if (Objects.isNull(resource)) {
      return Optional.empty();
    }
    BufferedReader reader = new BufferedReader(new InputStreamReader(resource));
    String scriptData = reader.lines().collect(joining(lineSeparator()));
    return Optional.of(scriptData);
  }
}

package com.okohub.azure.cosmosdb.junit;

import com.azure.cosmos.BulkProcessingOptions;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosItemOperation;
import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import reactor.core.publisher.Flux;

import static com.azure.cosmos.BulkOperations.getCreateItemOperation;

/**
 * @author onurozcan
 */
public class ResourceOperator {

  private final static ObjectMapper MAPPER = new ObjectMapper();

  private final CosmosAsyncClient cosmosClient;

  private final CosmosScript annotation;

  //

  private CosmosAsyncDatabase database;

  public ResourceOperator(CosmosAsyncClient cosmosClient, CosmosScript annotation) {
    this.cosmosClient = cosmosClient;
    this.annotation = annotation;
  }

  public ResourceOperator createDatabase() {
    String database = annotation.database();
    cosmosClient.createDatabaseIfNotExists(database).block();
    this.database = cosmosClient.getDatabase(database);
    return this;
  }

  public ResourceOperator deleteDatabase() {
    String database = annotation.database();
    cosmosClient.getDatabase(database).delete().block();
    return this;
  }

  public ResourceOperator createContainer() {
    String container = annotation.container();
    database.createContainerIfNotExists(container, "/" + annotation.partitionKey()).block();
    return this;
  }

  public ResourceOperator populate(Integer itemCount) {
    try {
      String data = readScript(annotation.script());
      JsonNode jsonNode = MAPPER.readTree(data);
      Iterable<JsonNode> iterable = jsonNode::elements;
      Stream<JsonNode> targetStream = StreamSupport.stream(iterable.spliterator(), false);
      Stream<CosmosItemOperation> operationStream =
          targetStream.limit(itemCount)
                      .map(node -> {
                        String key = node.findPath(annotation.partitionKey()).textValue();
                        PartitionKey partitionKey = new PartitionKey(key);
                        return getCreateItemOperation(node, partitionKey);
                      });
      Flux<CosmosItemOperation> operationFlux = Flux.fromStream(operationStream);
      BulkProcessingOptions bulkOptions = new BulkProcessingOptions();
      bulkOptions.setMaxMicroBatchSize(itemCount + 1);
      database.getContainer(annotation.container())
              .processBulkOperations(operationFlux, bulkOptions)
              .blockLast();
      return this;
    } catch (Exception ex) {
      throw new IllegalStateException(ex);
    }
  }

  private String readScript(String resourcePath) {
    InputStream resource = getClass().getResourceAsStream(resourcePath);
    BufferedReader reader = new BufferedReader(new InputStreamReader(resource));
    return reader.lines().collect(Collectors.joining(System.getProperty("line.separator")));
  }
}

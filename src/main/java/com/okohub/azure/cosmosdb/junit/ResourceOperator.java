package com.okohub.azure.cosmosdb.junit;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Optional;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    CosmosDbPopulator populator = findPopulator(annotation);
    Double totalRequestCharge = populator.populate(container, targetStream);
    LOGGER.info("Finished data population. Total request charge (RU): {}", totalRequestCharge);
  }

  private CosmosDbPopulator findPopulator(CosmosData annotation) {
    return annotation.useBulk()
        ? new CosmosDbBulkPopulator(annotation)
        : new CosmosDbSinglePopulator(annotation);
  }
}

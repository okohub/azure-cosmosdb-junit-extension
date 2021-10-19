package io.github.okohub.azure.cosmosdb.junit.sync;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosContainer;
import com.fasterxml.jackson.databind.JsonNode;
import io.github.okohub.azure.cosmosdb.junit.core.ResourceReader;
import java.util.Optional;
import java.util.stream.Stream;
import io.github.okohub.azure.cosmosdb.junit.CosmosData;
import io.github.okohub.azure.cosmosdb.junit.core.ResourceOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Onur Kagan Ozcan
 */
final class SyncResourceOperator implements ResourceOperator {

  private static final Logger LOGGER = LoggerFactory.getLogger(SyncResourceOperator.class);

  private final CosmosClient client;

  private final CosmosData annotation;

  private final ResourceReader resourceReader;

  private final SyncCosmosDBPopulator populator;

  public SyncResourceOperator(CosmosClient client,
                              CosmosData annotation,
                              ResourceReader resourceReader,
                              SyncCosmosDBPopulator populator) {
    this.client = client;
    this.annotation = annotation;
    this.resourceReader = resourceReader;
    this.populator = populator;
  }

  @Override
  public void createDatabase() {
    String database = annotation.database();
    client.createDatabaseIfNotExists(database);
  }

  @Override
  public void createContainer() {
    String database = annotation.database();
    String container = annotation.container();
    client.getDatabase(database)
          .createContainerIfNotExists(container, "/" + annotation.partitionKey());
  }

  @Override
  public void populate() throws Exception {
    String dataPath = annotation.path();
    Optional<String> dataContainer = resourceReader.readResource(dataPath);
    if (dataContainer.isEmpty()) {
      LOGGER.error("Can not populate because data is not found. DataPath: {}", dataPath);
      return;
    }
    CosmosContainer container = client.getDatabase(annotation.database())
                                      .getContainer(annotation.container());
    Stream<JsonNode> targetStream = resourceReader.readResourceContentAsJsonStream(dataContainer.get());
    //
    Double totalRequestCharge = populator.populate(container, targetStream);
    LOGGER.info("Finished data population. Total request charge (RU): {}", totalRequestCharge);
  }

  @Override
  public void deleteDatabase() {
    String database = annotation.database();
    client.getDatabase(database).delete();
  }
}

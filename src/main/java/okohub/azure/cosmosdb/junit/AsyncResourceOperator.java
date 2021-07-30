package okohub.azure.cosmosdb.junit;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Optional;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Onur Kagan Ozcan
 */
final class AsyncResourceOperator implements ResourceOperator {

  private static final Logger LOGGER = LoggerFactory.getLogger(AsyncResourceOperator.class);

  private final CosmosAsyncClient client;

  private final CosmosData annotation;

  private final ResourceReader resourceReader;

  private final AsyncCosmosDBPopulator populator;

  AsyncResourceOperator(CosmosAsyncClient client,
                        CosmosData annotation,
                        ResourceReader resourceReader,
                        AsyncCosmosDBPopulator populator) {
    this.client = client;
    this.annotation = annotation;
    this.resourceReader = resourceReader;
    this.populator = populator;
  }

  @Override
  public void createDatabase() {
    String database = annotation.database();
    client.createDatabaseIfNotExists(database).block();
  }

  @Override
  public void createContainer() {
    String database = annotation.database();
    String container = annotation.container();
    client.getDatabase(database)
          .createContainerIfNotExists(container, "/" + annotation.partitionKey())
          .block();
  }

  @Override
  public void populate() throws Exception {
    String dataPath = annotation.path();
    Optional<String> dataContainer = resourceReader.readResource(dataPath);
    if (dataContainer.isEmpty()) {
      LOGGER.error("Can not populate because data is not found. DataPath: {}", dataPath);
      return;
    }
    CosmosAsyncContainer container = client.getDatabase(annotation.database())
                                           .getContainer(annotation.container());
    Stream<JsonNode> targetStream = resourceReader.readResourceContentAsJsonStream(dataContainer.get());
    //
    Double totalRequestCharge = populator.populate(container, targetStream);
    LOGGER.info("Finished data population. Total request charge (RU): {}", totalRequestCharge);
  }

  @Override
  public void deleteDatabase() {
    String database = annotation.database();
    client.getDatabase(database).delete().block();
  }
}

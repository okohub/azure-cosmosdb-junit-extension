package okohub.azure.cosmosdb.junit.extra.testcontainers;

import okohub.azure.cosmosdb.junit.CosmosDataExtension;
import okohub.azure.cosmosdb.junit.CosmosDataExtensions;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.testcontainers.containers.CosmosDBEmulatorContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.testcontainers.containers.CosmosDBEmulatorContainer.LINUX_AZURE_COSMOS_DB_EMULATOR;
import static org.testcontainers.utility.DockerImageName.parse;

/**
 * Simple abstraction for ready-to-use emulator and client
 *
 * @author Onur Kagan Ozcan
 */
@Testcontainers
public class AbstractCosmosDBEmulatorWithSyncClientTest {

  @Container
  protected static final CosmosDBEmulatorContainer SHARED_CONTAINER =
      new CosmosDBEmulatorContainer(parse(LINUX_AZURE_COSMOS_DB_EMULATOR));

  @RegisterExtension
  protected CosmosDataExtension cosmosDataExtension =
      CosmosDataExtensions.withSyncClient(SHARED_CONTAINER.getEmulatorEndpoint(),
                                          SHARED_CONTAINER.getEmulatorLocalKey());
}

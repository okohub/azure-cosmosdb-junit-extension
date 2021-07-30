package okohub.azure.cosmosdb.junit.testcontainers;

import okohub.azure.cosmosdb.junit.CosmosDataExtension;
import okohub.azure.cosmosdb.junit.CosmosDataExtensions;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.testcontainers.containers.CosmosDbEmulatorContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.testcontainers.containers.CosmosDbEmulatorContainer.LINUX_AZURE_COSMOS_EMULATOR;
import static org.testcontainers.utility.DockerImageName.parse;

/**
 * Simple abstraction for ready-to-use emulator and client
 *
 * @author Onur Kagan Ozcan
 */
@Testcontainers
public class AbstractCosmosDbWithAsyncClientTest {

  @Container
  protected static final CosmosDbEmulatorContainer CONTAINER =
      new CosmosDbEmulatorContainer(parse(LINUX_AZURE_COSMOS_EMULATOR));

  @RegisterExtension
  protected CosmosDataExtension cosmosDataExtension = CosmosDataExtensions.async(CONTAINER.getEmulatorEndpoint(),
                                                                                 CONTAINER.getEmulatorLocalKey());
}

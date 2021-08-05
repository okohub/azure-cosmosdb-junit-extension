package okohub.azure.cosmosdb.junit.extra.testcontainers;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.CosmosDBEmulatorContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.fail;
import static org.testcontainers.utility.DockerImageName.parse;

/**
 * @author onurozcan
 */
@Testcontainers
public abstract class AbstractCosmosDBEmulatorTest {

  /**
   * Single container for all test methods.
   * Will be started in beforeAll callback.
   * @see org.testcontainers.junit.jupiter.TestcontainersExtension#beforeAll
   */
  @Container
  protected static final CosmosDBEmulatorContainer SHARED_CONTAINER =
      new CosmosDBEmulatorContainer(parse("mcr.microsoft.com/cosmosdb/linux/azure-cosmos-emulator"));

  @BeforeAll
  static void setUp() throws Exception {
    if (!SHARED_CONTAINER.isRunning()) {
      fail("Container must be running before initial setUp!");
    }
    Path keyStoreFile = Files.createTempFile("azure-cosmos-emulator", ".keystore");
    KeyStore keyStore = SHARED_CONTAINER.buildNewKeyStore();
    keyStore.store(new FileOutputStream(keyStoreFile.toFile()), SHARED_CONTAINER.getEmulatorKey().toCharArray());
    // Cosmos clients need certificate to call Cosmos DB
    System.setProperty("javax.net.ssl.trustStore", keyStoreFile.toString());
    System.setProperty("javax.net.ssl.trustStorePassword", SHARED_CONTAINER.getEmulatorKey());
    System.setProperty("javax.net.ssl.trustStoreType", "PKCS12");
  }
}

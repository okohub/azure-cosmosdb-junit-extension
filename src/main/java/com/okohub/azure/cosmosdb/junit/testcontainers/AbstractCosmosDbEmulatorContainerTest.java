package com.okohub.azure.cosmosdb.junit.testcontainers;

import com.okohub.azure.cosmosdb.junit.AsyncClientCosmosDataExtension;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.testcontainers.containers.CosmosDbEmulatorContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.testcontainers.containers.CosmosDbEmulatorContainer.LINUX_AZURE_COSMOS_EMULATOR;
import static org.testcontainers.utility.DockerImageName.parse;

/**
 * Simple abstraction for ready-to-use emulator and client
 *
 * @author onurozcan
 */
@Testcontainers
public class AbstractCosmosDbEmulatorContainerTest {

  @Container
  private static final CosmosDbEmulatorContainer CONTAINER =
      new CosmosDbEmulatorContainer(parse(LINUX_AZURE_COSMOS_EMULATOR));

  @RegisterExtension
  AsyncClientCosmosDataExtension cosmosDataExtension =
      new AsyncClientCosmosDataExtension(CONTAINER.getEmulatorEndpoint(), CONTAINER.getEmulatorLocalKey());
}

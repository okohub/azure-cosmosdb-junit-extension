package com.okohub.azure.cosmosdb.junit;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import java.util.HashMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.testcontainers.containers.CosmosDbEmulatorContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.containers.CosmosDbEmulatorContainer.LINUX_AZURE_COSMOS_EMULATOR;

/**
 * @author onurozcan
 */
@Testcontainers
public class AzureContainerCosmosScripTests {

  @Container
  private static final CosmosDbEmulatorContainer COSMOS_EMULATOR =
      new CosmosDbEmulatorContainer(DockerImageName.parse(LINUX_AZURE_COSMOS_EMULATOR));

  private final CosmosAsyncClient client = new CosmosClientBuilder().gatewayMode()
                                                                    .endpointDiscoveryEnabled(false)
                                                                    .endpoint(COSMOS_EMULATOR.getEmulatorEndpoint())
                                                                    .key(COSMOS_EMULATOR.getEmulatorLocalKey())
                                                                    .buildAsyncClient();

  @RegisterExtension
  AsyncCosmosScriptExtension cosmosScriptExtension = new AsyncCosmosScriptExtension(client);

  @CosmosScript(database = "mytest",
                container = "volcanos",
                script = "/volcano_data.json",
                partitionKey = "id")
  @Test
  public void shouldReadScriptFirstItemFromCosmosDb() {
    CosmosItemResponse<HashMap> response = client.getDatabase("mytest")
                                                 .getContainer("volcanos")
                                                 .readItem("4cb67ab0-ba1a-0e8a-8dfc-d48472fd5766",
                                                           new PartitionKey("4cb67ab0-ba1a-0e8a-8dfc-d48472fd5766"),
                                                           HashMap.class)
                                                 .block();
    assertThat(response.getStatusCode()).isEqualTo(200);
    HashMap item = response.getItem();
    assertThat(item).hasSize(14);
  }

  @Test
  public void shouldDoNothing() {
    System.out.println("nothing");
  }
}
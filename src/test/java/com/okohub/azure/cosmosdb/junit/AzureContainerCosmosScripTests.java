package com.okohub.azure.cosmosdb.junit;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosException;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.testcontainers.containers.CosmosDbEmulatorContainer.LINUX_AZURE_COSMOS_EMULATOR;

/**
 * @author onurozcan
 */
@Testcontainers
public class AzureContainerCosmosScripTests {

  @Container
  private static final CosmosDbEmulatorContainer COSMOS_EMULATOR =
      new CosmosDbEmulatorContainer(DockerImageName.parse(LINUX_AZURE_COSMOS_EMULATOR));

  @RegisterExtension
  AsyncCosmosScriptExtension cosmosScriptExtension =
      new AsyncCosmosScriptExtension(COSMOS_EMULATOR.getEmulatorEndpoint(),
                                     COSMOS_EMULATOR.getEmulatorLocalKey());

  @CosmosScript(database = "mytest",
                container = "volcanos",
                script = "/volcano_data.json",
                partitionKey = "id")
  @Test
  public void shouldReadScriptFirstItemFromCosmosDb(CosmosAsyncClient client) {
    String firstItemId = "4cb67ab0-ba1a-0e8a-8dfc-d48472fd5766";
    CosmosItemResponse<HashMap> firstItemResponse = client.getDatabase("mytest")
                                                          .getContainer("volcanos")
                                                          .readItem(firstItemId,
                                                                    new PartitionKey(firstItemId),
                                                                    HashMap.class)
                                                          .block();
    assertThat(firstItemResponse.getStatusCode()).isEqualTo(200);
    HashMap firstItem = firstItemResponse.getItem();
    assertThat(firstItem).hasSize(14);
    assertThat(firstItem).containsEntry("id", firstItemId);
  }

  @Test
  public void shouldNotReadScriptFirstItemFromCosmosDbBecauseOfNonexistence(CosmosAsyncClient client) {
    assertThrows(CosmosException.class, () -> {
      String firstItemId = "4cb67ab0-ba1a-0e8a-8dfc-d48472fd5766";
      client.getDatabase("mytest")
            .getContainer("volcanos")
            .readItem(firstItemId,
                      new PartitionKey(firstItemId),
                      HashMap.class)
            .block();
    });
  }
}

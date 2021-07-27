package com.okohub.azure.cosmosdb.junit;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.okohub.azure.cosmosdb.junit.testcontainers.AbstractCosmosDbEmulatorContainerTest;
import java.util.HashMap;
import org.junit.jupiter.api.Test;

import static com.okohub.azure.cosmosdb.junit.Constants.DEFAULT_CONTAINER;
import static com.okohub.azure.cosmosdb.junit.Constants.DEFAULT_DATABASE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author onurozcan
 */
public class CosmosDbEmulatorContainerCosmosDataTests extends AbstractCosmosDbEmulatorContainerTest {

  @CosmosData(path = "volcano_data_big.json", partitionKey = "id", useBulk = true)
  @Test
  public void shouldReadFirstDataItemFromBigData(CosmosAsyncClient client) {
    String firstItemId = "4cb67ab0-ba1a-0e8a-8dfc-d48472fd5766";
    CosmosItemResponse<HashMap> firstItemResponse = client.getDatabase(DEFAULT_DATABASE)
                                                          .getContainer(DEFAULT_CONTAINER)
                                                          .readItem(firstItemId,
                                                                    new PartitionKey(firstItemId),
                                                                    HashMap.class)
                                                          .block();
    assertThat(firstItemResponse.getStatusCode()).isEqualTo(200);
    HashMap firstItem = firstItemResponse.getItem();
    assertThat(firstItem).hasSize(14);
    assertThat(firstItem).containsEntry("id", firstItemId);
  }

  @CosmosData(path = "volcano_data_small.json", partitionKey = "id")
  @Test
  public void shouldReadFirstDataItemFromSmallData(CosmosAsyncClient client) {
    String firstItemId = "4cb67ab0-ba1a-0e8a-8dfc-d48472fd5766";
    CosmosItemResponse<HashMap> firstItemResponse = client.getDatabase(DEFAULT_DATABASE)
                                                          .getContainer(DEFAULT_CONTAINER)
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
  public void shouldNotReadFirstDataItemFromCosmosDbBecauseOfNonexistence(CosmosAsyncClient client) {
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

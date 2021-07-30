package okohub.azure.cosmosdb.junit;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.CosmosItemOperationType;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import java.util.HashMap;
import okohub.azure.cosmosdb.junit.testcontainers.AbstractCosmosDbWithSyncClientTest;
import org.junit.jupiter.api.Test;

import static okohub.azure.cosmosdb.junit.Constants.DEFAULT_CONTAINER;
import static okohub.azure.cosmosdb.junit.Constants.DEFAULT_DATABASE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Onur Kagan Ozcan
 */
public class CosmosDataWithSyncClientTests extends AbstractCosmosDbWithSyncClientTest {

  @CosmosData(path = "volcano_data_big.json", partitionKey = "id", useBulk = true)
  @Test
  public void shouldReadFirstDataItemFromBigData(CosmosClient client) {
    String firstItemId = "4cb67ab0-ba1a-0e8a-8dfc-d48472fd5766";
    CosmosItemResponse<HashMap> firstItemResponse = client.getDatabase(DEFAULT_DATABASE)
                                                          .getContainer(DEFAULT_CONTAINER)
                                                          .readItem(firstItemId,
                                                                    new PartitionKey(firstItemId),
                                                                    HashMap.class);
    assertThat(firstItemResponse.getStatusCode()).isEqualTo(200);
    HashMap firstItem = firstItemResponse.getItem();
    assertThat(firstItem).hasSize(14);
    assertThat(firstItem).containsEntry("id", firstItemId);
  }

  @CosmosData(path = "volcano_data_small.json",
              partitionKey = "id",
              operationType = CosmosItemOperationType.UPSERT)
  @Test
  public void shouldReadFirstDataItemFromSmallData(CosmosClient client) {
    String firstItemId = "4cb67ab0-ba1a-0e8a-8dfc-d48472fd5766";
    CosmosItemResponse<HashMap> firstItemResponse = client.getDatabase(DEFAULT_DATABASE)
                                                          .getContainer(DEFAULT_CONTAINER)
                                                          .readItem(firstItemId,
                                                                    new PartitionKey(firstItemId),
                                                                    HashMap.class);
    assertThat(firstItemResponse.getStatusCode()).isEqualTo(200);
    HashMap firstItem = firstItemResponse.getItem();
    assertThat(firstItem).hasSize(14);
    assertThat(firstItem).containsEntry("id", firstItemId);
  }

  @Test
  public void shouldNotReadFirstDataItemFromCosmosDbBecauseOfNonexistence(CosmosClient client) {
    assertThrows(CosmosException.class, () -> {
      String firstItemId = "4cb67ab0-ba1a-0e8a-8dfc-d48472fd5766";
      client.getDatabase("mytest")
            .getContainer("volcanos")
            .readItem(firstItemId,
                      new PartitionKey(firstItemId),
                      HashMap.class);
    });
  }
}

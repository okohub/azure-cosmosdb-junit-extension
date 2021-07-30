package okohub.azure.cosmosdb.junit;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.CosmosItemOperationType;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import okohub.azure.cosmosdb.junit.testcontainers.AbstractCosmosDbWithAsyncClientTest;
import java.util.HashMap;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Onur Kagan Ozcan
 */
public class CosmosDataWithAsyncClientTests extends AbstractCosmosDbWithAsyncClientTest {

  @CosmosData(path = "volcano_data_big.json", partitionKey = "id", useBulk = true)
  @Test
  public void shouldReadFirstDataItemFromBigData(CosmosAsyncClient client) {
    String firstItemId = "4cb67ab0-ba1a-0e8a-8dfc-d48472fd5766";
    CosmosItemResponse<HashMap> firstItemResponse = client.getDatabase(Constants.DEFAULT_DATABASE)
                                                          .getContainer(Constants.DEFAULT_CONTAINER)
                                                          .readItem(firstItemId,
                                                                    new PartitionKey(firstItemId),
                                                                    HashMap.class)
                                                          .block();
    assertThat(firstItemResponse.getStatusCode()).isEqualTo(200);
    HashMap firstItem = firstItemResponse.getItem();
    assertThat(firstItem).hasSize(14);
    assertThat(firstItem).containsEntry("id", firstItemId);
  }

  @CosmosData(path = "volcano_data_small.json",
              partitionKey = "id",
              operationType = CosmosItemOperationType.UPSERT)
  @Test
  public void shouldReadFirstDataItemFromSmallData(CosmosAsyncClient client) {
    String firstItemId = "4cb67ab0-ba1a-0e8a-8dfc-d48472fd5766";
    CosmosItemResponse<HashMap> firstItemResponse = client.getDatabase(Constants.DEFAULT_DATABASE)
                                                          .getContainer(Constants.DEFAULT_CONTAINER)
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

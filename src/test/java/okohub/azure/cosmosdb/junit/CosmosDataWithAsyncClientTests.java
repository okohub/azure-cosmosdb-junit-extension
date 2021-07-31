package okohub.azure.cosmosdb.junit;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.CosmosItemOperationType;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import java.util.HashMap;
import okohub.azure.cosmosdb.junit.extra.testcontainers.AbstractCosmosDBEmulatorWithAsyncClientTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Onur Kagan Ozcan
 */
public class CosmosDataWithAsyncClientTests extends AbstractCosmosDBEmulatorWithAsyncClientTest {

  @CosmosData(path = "volcano_data_big.json", partitionKey = "id", useBulk = true)
  @Test
  public void shouldReadFirstDataItemFromBigData(CosmosAsyncClient client) {
    String firstItemId = "4cb67ab0-ba1a-0e8a-8dfc-d48472fd5766";
    CosmosItemResponse<HashMap> firstItemResponse = getDefaultContainer(client).readItem(firstItemId,
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
    CosmosItemResponse<HashMap> firstItemResponse = getDefaultContainer(client).readItem(firstItemId,
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
      getDefaultContainer(client).readItem(firstItemId,
                                           new PartitionKey(firstItemId),
                                           HashMap.class)
                                 .block();
    });
  }

  private CosmosAsyncContainer getDefaultContainer(CosmosAsyncClient client) {
    return client.getDatabase("COSMOS_DB_EMULATOR_DATABASE")
                 .getContainer("COSMOS_DB_EMULATOR_CONTAINER");
  }
}

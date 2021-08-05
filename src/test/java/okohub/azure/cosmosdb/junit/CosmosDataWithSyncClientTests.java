package okohub.azure.cosmosdb.junit;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.CosmosItemOperationType;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import java.util.HashMap;
import okohub.azure.cosmosdb.junit.extra.testcontainers.AbstractCosmosDBEmulatorTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static okohub.azure.cosmosdb.junit.CosmosDataExtensions.withSyncClient;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Onur Kagan Ozcan
 */
public class CosmosDataWithSyncClientTests extends AbstractCosmosDBEmulatorTest {

  /**
   * This extension can not be used static, because container must be started before.
   * That's why, extension is instance field.
   * It means a fresh client is recreated and destroyed for every test method.
   */
  @RegisterExtension
  protected CosmosDataExtension cosmosDataExtension = withSyncClient(SHARED_CONTAINER.getEmulatorEndpoint(),
                                                                     SHARED_CONTAINER.getEmulatorKey());

  @CosmosData(path = "volcano_data_big.json", partitionKey = "id", useBulk = true)
  @Test
  public void shouldReadFirstDataItemFromBigData(CosmosClient client) {
    String firstItemId = "4cb67ab0-ba1a-0e8a-8dfc-d48472fd5766";
    CosmosItemResponse<HashMap> firstItemResponse = getDefaultContainer(client).readItem(firstItemId,
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
    CosmosItemResponse<HashMap> firstItemResponse = getDefaultContainer(client).readItem(firstItemId,
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
      getDefaultContainer(client).readItem(firstItemId,
                                           new PartitionKey(firstItemId),
                                           HashMap.class);
    });
  }

  private CosmosContainer getDefaultContainer(CosmosClient client) {
    return client.getDatabase("COSMOS_DB_EMULATOR_DATABASE")
                 .getContainer("COSMOS_DB_EMULATOR_CONTAINER");
  }
}

package io.github.okohub.azure.cosmosdb.junit;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CosmosItemOperationType;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import io.github.okohub.azure.cosmosdb.junit.testcontainers.AbstractCosmosDBEmulatorTest;
import java.util.HashMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static io.github.okohub.azure.cosmosdb.junit.CosmosDataExtensions.withAsyncClient;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Onur Kagan Ozcan
 */
public class CosmosDataWithAsyncClientTests extends AbstractCosmosDBEmulatorTest {

  /**
   * This extension can not be used static, because container must be started before.
   * That's why, extension is instance field.
   * It means a fresh client is recreated and destroyed for every test method.
   */
  @RegisterExtension
  CosmosDataExtension cosmosDataExtension = withAsyncClient(SHARED_CONTAINER.getEmulatorEndpoint(),
                                                            SHARED_CONTAINER.getEmulatorKey());

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

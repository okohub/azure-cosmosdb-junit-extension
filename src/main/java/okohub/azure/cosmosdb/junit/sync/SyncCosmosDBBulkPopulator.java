package okohub.azure.cosmosdb.junit.sync;

import com.azure.cosmos.CosmosBulkOperationResponse;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosItemOperation;
import com.azure.cosmos.CosmosItemOperationType;
import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import okohub.azure.cosmosdb.junit.CosmosData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.azure.cosmos.BulkOperations.getCreateItemOperation;
import static com.azure.cosmos.BulkOperations.getReplaceItemOperation;
import static com.azure.cosmos.BulkOperations.getUpsertItemOperation;
import static com.azure.cosmos.implementation.batch.BatchRequestResponseConstants.MAX_OPERATIONS_IN_DIRECT_MODE_BATCH_REQUEST;

/**
 * @author Onur Kagan Ozcan
 */
final class SyncCosmosDBBulkPopulator implements SyncCosmosDBPopulator {

  private static final Logger LOGGER = LoggerFactory.getLogger(SyncCosmosDBBulkPopulator.class);

  private final CosmosData annotation;

  SyncCosmosDBBulkPopulator(CosmosData annotation) {
    this.annotation = annotation;
  }

  @Override
  public Double populate(CosmosContainer container, Stream<JsonNode> targetStream) {
    int bulkChunkSize = Math.min(annotation.bulkChunkSize(), MAX_OPERATIONS_IN_DIRECT_MODE_BATCH_REQUEST);
    List<JsonNode> allNodes = targetStream.toList();
    return Partition.ofSize(allNodes, bulkChunkSize)
                    .stream()
                    .map(part -> doBulkOperation(container, part, annotation.operationType()))
                    .flatMap(Collection::stream)
                    .map(response -> response.getResponse().getRequestCharge())
                    .reduce(0.0, Double::sum);
  }

  private List<CosmosBulkOperationResponse<Object>> doBulkOperation(CosmosContainer container,
                                                                    List<JsonNode> jsonNodes,
                                                                    CosmosItemOperationType opType) {
    LOGGER.info("Creating new chunk for bulk operation. Size: {}", jsonNodes.size());
    var responses = container.processBulkOperations(jsonNodes.stream()
                                                             .map(jn -> newBulkItemOperation(jn, opType))
                                                             .toList());
    LOGGER.info("Finished single chunk. Size: {}", jsonNodes.size());
    return responses;
  }

  private CosmosItemOperation newBulkItemOperation(JsonNode jn, CosmosItemOperationType opType) {
    String key = jn.findPath(annotation.partitionKey()).textValue();
    PartitionKey partitionKey = new PartitionKey(key);
    return switch (opType) {
      case REPLACE -> getReplaceItemOperation(jn.findPath(annotation.idKey()).textValue(), jn, partitionKey);
      case UPSERT -> getUpsertItemOperation(jn, partitionKey);
      default -> getCreateItemOperation(jn, partitionKey);
    };
  }

  private static class Partition<T> extends AbstractList<List<T>> {

    private final List<T> list;

    private final int chunkSize;

    private Partition(List<T> list, int chunkSize) {
      this.list = new ArrayList<>(list);
      this.chunkSize = chunkSize;
    }

    static <T> Partition<T> ofSize(List<T> list, int chunkSize) {
      return new Partition<>(list, chunkSize);
    }

    @Override
    public List<T> get(int index) {
      int start = index * chunkSize;
      int end = Math.min(start + chunkSize, list.size());

      if (start > end) {
        throw new IndexOutOfBoundsException("Index " + index + " is out of the list range <0," + (size() - 1) + ">");
      }

      return new ArrayList<>(list.subList(start, end));
    }

    @Override
    public int size() {
      return (int) Math.ceil((double) list.size() / (double) chunkSize);
    }
  }
}

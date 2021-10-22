package io.github.okohub.azure.cosmosdb.junit.core;

import com.azure.cosmos.models.CosmosBulkOperations;
import com.azure.cosmos.models.CosmosItemOperation;
import com.azure.cosmos.models.CosmosItemOperationType;
import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.databind.JsonNode;
import io.github.okohub.azure.cosmosdb.junit.CosmosData;

/**
 * @author onurozcan
 */
public final class BulkItemOperationCreator {

  public static CosmosItemOperation newOperation(CosmosData annotation,
                                                 JsonNode jn,
                                                 CosmosItemOperationType opType) {
    String key = jn.findPath(annotation.partitionKey()).textValue();
    PartitionKey partitionKey = new PartitionKey(key);
    return switch (opType) {
      case REPLACE -> CosmosBulkOperations.getReplaceItemOperation(jn.findPath(annotation.idKey()).textValue(),
                                                                   jn,
                                                                   partitionKey);
      case UPSERT -> CosmosBulkOperations.getUpsertItemOperation(jn, partitionKey);
      default -> CosmosBulkOperations.getCreateItemOperation(jn, partitionKey);
    };
  }
}

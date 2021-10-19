package io.github.okohub.azure.cosmosdb.junit.sync;

import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.implementation.batch.BatchRequestResponseConstants;
import com.azure.cosmos.models.CosmosBulkOperationResponse;
import com.azure.cosmos.models.CosmosItemOperation;
import com.azure.cosmos.models.CosmosItemOperationType;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import io.github.okohub.azure.cosmosdb.junit.CosmosData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.github.okohub.azure.cosmosdb.junit.core.BulkItemOperationCreator.newOperation;

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
    int bulkChunkSize = Math.min(annotation.bulkChunkSize(),
                                 BatchRequestResponseConstants.MAX_OPERATIONS_IN_DIRECT_MODE_BATCH_REQUEST);
    List<JsonNode> allNodes = targetStream.toList();
    return Partitioner.ofSize(allNodes, bulkChunkSize)
                      .stream()
                      .map(part -> doBulkOperation(container, part, annotation.operationType()))
                      .flatMap(iter -> StreamSupport.stream(iter.spliterator(), false))
                      .map(response -> response.getResponse().getRequestCharge())
                      .reduce(0.0, Double::sum);
  }

  private Iterable<CosmosBulkOperationResponse<Object>> doBulkOperation(CosmosContainer container,
                                                                        List<JsonNode> jsonNodes,
                                                                        CosmosItemOperationType opType) {
    LOGGER.info("Creating new chunk for bulk operation. Size: {}", jsonNodes.size());
    List<CosmosItemOperation> operations = jsonNodes.stream()
                                                    .map(jn -> newOperation(annotation, jn, opType))
                                                    .toList();
    var responses = container.executeBulkOperations(operations);
    LOGGER.info("Finished single chunk. Size: {}", jsonNodes.size());
    return responses;
  }
}

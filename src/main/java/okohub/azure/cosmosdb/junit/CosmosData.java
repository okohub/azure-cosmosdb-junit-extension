package okohub.azure.cosmosdb.junit;

import com.azure.cosmos.CosmosItemOperationType;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Onur Kagan Ozcan
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CosmosData {

  /**
   * If path starts with "/", will be resolved from direct path
   * If path not starts with "/", will be resolved from resources
   *
   * @return path of data
   */
  String path();

  String database() default Constants.DEFAULT_DATABASE;

  String container() default Constants.DEFAULT_CONTAINER;

  /**
   * currently supported for:
   * CREATE (default)
   * REPLACE -> when using replace, keep in mind that there must be an existing data
   * UPSERT
   */
  CosmosItemOperationType operationType() default CosmosItemOperationType.CREATE;

  boolean useBulk() default false;

  /**
   * this parameter cannot be bigger than Azure defaults
   *
   * @see com.azure.cosmos.implementation.batch.BatchRequestResponseConstants#MAX_OPERATIONS_IN_DIRECT_MODE_BATCH_REQUEST
   */
  int bulkChunkSize() default Constants.DEFAULT_BULK_CHUNK_SIZE;

  String idKey() default Constants.DEFAULT_ID_KEY;

  String partitionKey();
}

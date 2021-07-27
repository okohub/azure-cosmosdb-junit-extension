package com.okohub.azure.cosmosdb.junit;

import com.azure.cosmos.CosmosItemOperationType;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author onurozcan
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

  boolean useBulk() default false;

  /**
   * currently supported for:
   * CREATE (default)
   * REPLACE
   * UPSERT
   */
  CosmosItemOperationType bulkOperationType() default CosmosItemOperationType.CREATE;

  /**
   * this parameter cannot be bigger than Azure defaults
   *
   * @see com.azure.cosmos.implementation.batch.BatchRequestResponseConstants#MAX_OPERATIONS_IN_DIRECT_MODE_BATCH_REQUEST
   */
  int bulkChunkSize() default Constants.DEFAULT_CHUNK_SIZE;

  String idKey() default Constants.DEFAULT_ID_KEY;

  String partitionKey();
}

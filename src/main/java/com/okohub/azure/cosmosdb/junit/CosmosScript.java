package com.okohub.azure.cosmosdb.junit;

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
public @interface CosmosScript {

  String database() default Constants.DEFAULT_DATABASE;

  String container() default Constants.DEFAULT_CONTAINER;

  /**
   * If script path starts with "/", will be resolved from direct path
   * If script path not starts with "/", will be resolved from resources
   *
   * @return path of script
   */
  String script();

  /**
   * this parameter cannot be bigger than Azure defaults
   *
   * @see com.azure.cosmos.implementation.batch.BatchRequestResponseConstants#MAX_OPERATIONS_IN_DIRECT_MODE_BATCH_REQUEST
   */
  int chunkSize() default Constants.DEFAULT_CHUNK_SIZE;

  String partitionKey();
}

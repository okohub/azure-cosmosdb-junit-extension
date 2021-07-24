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

  String script();

  int chunkSize() default Constants.DEFAULT_CHUNK_SIZE;

  String partitionKey();
}

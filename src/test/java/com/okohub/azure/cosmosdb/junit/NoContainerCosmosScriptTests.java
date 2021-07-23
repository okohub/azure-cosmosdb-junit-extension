package com.okohub.azure.cosmosdb.junit;

import org.junit.jupiter.api.Test;

/**
 * @author onurozcan
 * https://github.com/Azure-Samples/azure-cosmos-db-sample-data/blob/main/SampleData/VolcanoData.json
 */
public class NoContainerCosmosScriptTests {

  @CosmosScript(database = "mytest",
                container = "volcanos",
                script = "/volcano_data.json",
                partitionKey = "id")
  @Test
  public void hey1() {
    System.out.println("hey1");
  }

  @Test
  public void hey2() {
    System.out.println("hey2");
  }
}

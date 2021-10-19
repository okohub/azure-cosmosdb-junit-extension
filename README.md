# Azure Cosmos DB Junit(5) Extension

I created this extension after introducing Azure Cosmos DB testcontainers plugin.

See https://github.com/testcontainers/testcontainers-java/pull/4303

## Usage

CosmosDataExtensions class is entry point for extensions.

You can use sync or async extensions, both will configure proper client for you.

````java
import com.azure.cosmos.CosmosAsyncClient;
import io.github.okohub.azure.cosmosdb.junit.CosmosData;
import io.github.okohub.azure.cosmosdb.junit.CosmosDataExtension;
import io.github.okohub.azure.cosmosdb.junit.CosmosDataExtensions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class MyAwesomeTests /* extends AbstractCosmosDBEmulatorTest */ {

  @RegisterExtension
  CosmosDataExtension cosmosDataExtension = CosmosDataExtensions.withAsyncClient("endpoint", "key");

  @CosmosData(path = "data.json", partitionKey = "id")
  @Test
  public void shouldDoSomething(CosmosAsyncClient client) {
    //do something
  }
}
````

## Features

- Support for data load: Just from your test resources or absolute path.
- Autoconfiguration for clients: Just inject to your test method. Neat!
- Sensible defaults for minimum code: You don't need to provide every detail. Just test your code!
- Optional testcontainers abstractions for tests: Just provide necessary dependencies.

## License

Azure Cosmos DB Junit Extension is licensed under the [MIT](/LICENSE.md) license.

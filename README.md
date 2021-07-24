# Azure CosmosDb Junit(5) Extension

I created this extension after introducing Azure CosmosDb testcontainers plugin.

WARNING!

This project is still in WIP! After new testcontainers release, project will be in its own release cycle.

See https://github.com/testcontainers/testcontainers-java/pull/4303

## Usage
````java
public class MyAwesomeTests {
  
  //here may be some awesome testcontainers code, you can check module tests!

  @RegisterExtension
  AsyncClientCosmosScriptExtension cosmosScriptExtension =
      new AsyncClientCosmosScriptExtension("endpoint", "key");

  @CosmosScript(script = "data.json", partitionKey = "id")
  @Test
  public void shouldDoSomething(CosmosAsyncClient client) {
  //do something
  }
}
````
## Features

- Supported loading data from resources or absolute path.
- Use autoconfigured cool CosmosAsyncClient by injecting to test method. Neat!
- `@CosmosScript` annotation has sensible defaults, so you don't need to provide every detail. Just test your code!

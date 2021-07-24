package com.okohub.azure.cosmosdb.junit;

import java.nio.file.Path;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author onurozcan
 */
public class ResourceReaderTests {

  @Test
  public void shouldFindResourceByResources() {
    ResourceReader resourceReader = new ResourceReader();
    Optional<String> dataContainer = resourceReader.readResource("volcano_data.json");
    Assertions.assertThat(dataContainer).isNotEmpty();
  }

  @Test
  public void shouldNotFindResourceByResources() {
    ResourceReader resourceReader = new ResourceReader();
    Optional<String> dataContainer = resourceReader.readResource("foo.unknown");
    Assertions.assertThat(dataContainer).isEmpty();
  }

  @Test
  public void shouldFindResourceByDirectPath() {
    ResourceReader resourceReader = new ResourceReader();
    String pwd = Path.of("").toAbsolutePath().toString();
    Optional<String> dataContainer = resourceReader.readResource(pwd + "/pom.xml");
    Assertions.assertThat(dataContainer).isNotEmpty();
  }

  @Test
  public void shouldNotFindResourceByDirectPath() {
    ResourceReader resourceReader = new ResourceReader();
    String pwd = Path.of("").toAbsolutePath().toString();
    Optional<String> dataContainer = resourceReader.readResource(pwd + "/bar.xml");
    Assertions.assertThat(dataContainer).isEmpty();
  }
}

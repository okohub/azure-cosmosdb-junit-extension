package io.github.okohub.azure.cosmosdb.junit;

import io.github.okohub.azure.cosmosdb.junit.core.ResourceReader;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Onur Kagan Ozcan
 */
public class ResourceReaderTests {

  @Test
  public void shouldFindResourceByResources() {
    ResourceReader resourceReader = new ResourceReader();
    Optional<String> dataContainer = resourceReader.readResource("volcano_data_small.json");
    assertThat(dataContainer).isNotEmpty();
  }

  @Test
  public void shouldNotFindResourceByResources() {
    ResourceReader resourceReader = new ResourceReader();
    Optional<String> dataContainer = resourceReader.readResource("foo.unknown");
    assertThat(dataContainer).isEmpty();
  }

  @Test
  public void shouldFindResourceByDirectPath() {
    ResourceReader resourceReader = new ResourceReader();
    String pwd = Path.of("").toAbsolutePath().toString();
    Optional<String> dataContainer = resourceReader.readResource(pwd + "/pom.xml");
    assertThat(dataContainer).isNotEmpty();
  }

  @Test
  public void shouldNotFindResourceByDirectPath() {
    ResourceReader resourceReader = new ResourceReader();
    String pwd = Path.of("").toAbsolutePath().toString();
    Optional<String> dataContainer = resourceReader.readResource(pwd + "/bar.xml");
    assertThat(dataContainer).isEmpty();
  }
}

package io.github.okohub.azure.cosmosdb.junit.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.joining;

/**
 * @author Onur Kagan Ozcan
 */
public final class ResourceReader {

  private final static ObjectMapper MAPPER = new ObjectMapper();

  public Optional<String> readResource(String resourcePath) {
    if (resourcePath.startsWith("/")) {
      return readResourceFromPath(resourcePath);
    }
    return readResourceFromResources(resourcePath);
  }

  private Optional<String> readResourceFromResources(String resourcePath) {
    InputStream resource = getClass().getResourceAsStream("/" + resourcePath);
    if (Objects.isNull(resource)) {
      return Optional.empty();
    }
    return getResourceAsString(resource);
  }

  private Optional<String> readResourceFromPath(String resourcePath) {
    File file = Path.of(resourcePath).toFile();
    if (!file.exists()) {
      return Optional.empty();
    }
    try (FileInputStream resource = new FileInputStream(file)) {
      return getResourceAsString(resource);
    } catch (Exception ex) {
      return Optional.empty();
    }
  }

  private Optional<String> getResourceAsString(InputStream resource) {
    BufferedReader reader = new BufferedReader(new InputStreamReader(resource));
    String data = reader.lines().collect(joining(lineSeparator()));
    return Optional.of(data);
  }

  public Stream<JsonNode> readResourceContentAsJsonStream(String data) throws JsonProcessingException {
    JsonNode jsonNode = MAPPER.readTree(data);
    Iterable<JsonNode> iterable = jsonNode::elements;
    return StreamSupport.stream(iterable.spliterator(), false);
  }
}

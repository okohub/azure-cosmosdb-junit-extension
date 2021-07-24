package com.okohub.azure.cosmosdb.junit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.joining;

/**
 * @author onurozcan
 */
final class ResourceReader {

  private final static ObjectMapper MAPPER = new ObjectMapper();

  Optional<String> readResource(String resourcePath) {
    InputStream resource = getClass().getResourceAsStream(resourcePath);
    if (Objects.isNull(resource)) {
      return Optional.empty();
    }
    BufferedReader reader = new BufferedReader(new InputStreamReader(resource));
    String scriptData = reader.lines().collect(joining(lineSeparator()));
    return Optional.of(scriptData);
  }

  Stream<JsonNode> readResourceContentAsJsonStream(String resourceContent) throws JsonProcessingException {
    JsonNode jsonNode = MAPPER.readTree(resourceContent);
    Iterable<JsonNode> iterable = jsonNode::elements;
    return StreamSupport.stream(iterable.spliterator(), false);
  }
}

package com.okohub.azure.cosmosdb.junit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.joining;

/**
 * @author onurozcan
 */
public class FluxExamples {

  @Test
  public void tryFlux() throws JsonProcessingException {
    String scriptData = readScript("/volcano_data.json").get();
    JsonNode jsonNode = new ObjectMapper().readTree(scriptData);
    Iterable<JsonNode> iterable = jsonNode::elements;
    Stream<JsonNode> targetStream = StreamSupport.stream(iterable.spliterator(), false);

    Flux.fromStream(targetStream)
        .flatMap((Function<JsonNode, Publisher<?>>) jsonNode1 -> Mono.just(jsonNode1.findPath("id").textValue()))
        .buffer(Duration.ofMillis(10))
        .doOnNext(objects -> {
          System.out.println("got a batch. size: {}" + +objects.size());
          System.out.println(objects);
        }).subscribe();
  }

  private Optional<String> readScript(String resourcePath) {
    InputStream resource = getClass().getResourceAsStream(resourcePath);
    if (Objects.isNull(resource)) {
      return Optional.empty();
    }
    BufferedReader reader = new BufferedReader(new InputStreamReader(resource));
    String scriptData = reader.lines().collect(joining(lineSeparator()));
    return Optional.of(scriptData);
  }
}

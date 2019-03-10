package com.github.daggerok.eventsourcedapp;

import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.vavr.collection.HashMap.empty;
import static java.lang.String.format;
import static java.util.Collections.singletonMap;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Configuration
@RequiredArgsConstructor
public class WebFluxConfig {

  final EventStore eventStore;
  final AggregateRepository repository;

  @PostConstruct
  public void init() {
    AtomicLong counter = new AtomicLong(1);
    UUID aggregateId = UUID.randomUUID();
    repository.deleteAll()
              .then(eventStore.deleteAll())
              .thenMany(Flux.fromStream(Stream.of("one", "two", "three"))
                            .map(s -> new DomainEvent(null, Instant.now(), singletonMap("payload", s)))
                            .flatMap(eventStore::save))
              //// comment next line out to take a chance see versioning of the aggregate:
              //.then(eventStore.count())
              .subscribe(whoCares -> {
                eventStore.count().subscribe();
                List<DomainEvent> events = eventStore.findAll().toStream().collect(Collectors.toList());
                repository.findByAggregateId(aggregateId)
                          .defaultIfEmpty(new Aggregate(null, null, aggregateId, new ArrayList<>()))
                          .map(aggregate -> {
                            aggregate.getEventStream().addAll(events);
                            return aggregate;
                          })
                          .flatMap(repository::save)
                          .subscribe(aggregate -> System.out.println("\n " + counter.getAndIncrement() + ") " + aggregate));
                Try.run(() -> Thread.sleep(222));
              });
  }

  @Bean
  RouterFunction routes() {
    return nest(path("/"),
                route(GET("/events"), req -> ok().body(eventStore.findAll(), DomainEvent.class)).and(
                route(GET("/aggregates"), req -> ok().body(repository.findAll(), Aggregate.class)).and(
                route(GET("/**"), req -> ok().body(Mono.just(singletonMap(
                    "links", empty().put("events: GET", toUrl(req, "/events"))
                                    .put("aggregates: GET", toUrl(req, "/aggregates"))
                                    .put("_self: GET", toUrl(req, req.path()))
                                    .toJavaMap())), Map.class)))));
  }

  private String toUrl(ServerRequest req, String path) {
    Objects.requireNonNull(req, "req argument is required.");
    return format("%s://%s%s", req.uri().getScheme(), req.uri().getAuthority(), path);
  }
}

package com.github.daggerok.eventsourcedapp

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

@Document
data class DomainEvent(
    @Id val id: String? = null,
    val createdAt: Instant? = Instant.now(),
    val data: Any
)

@Document
data class Aggregate(
    @Id val id: String? = null,
    @Version val revisionNumber: Int? = 0,
    val aggregateId: UUID? = UUID.randomUUID(),
    val eventStream: List<DomainEvent> = mutableListOf()
)

interface EventStore : ReactiveMongoRepository<DomainEvent, String>

interface AggregateRepository : ReactiveMongoRepository<Aggregate, String> {
  fun findByAggregateId(aggregateId: UUID): Mono<Aggregate>
}

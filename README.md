# EventSourced
Play with Event Sourcing and CQRS (in progress)

TODO:

- ~~implement DomainEvent entity / store~~
- ~~implement Aggregate entity / store~~
- implement http command side REST API
  - create new aggregate, or:
  - find aggregate by id via recreating it from existing events
  - validate command
  - if all OK, create event in event store
  - apply event to the aggregate (mainly increase it's version number for validity) and push back to aggregate store
- implement query side REST API not optimized, get and rebuild entity state completely from scratch
- after, introduce messaging layer and subscribe all queries as projections

```bash
./mvnw clean spring-boot:run
http :8080/
http :8080/events
http :8080/aggregates
```

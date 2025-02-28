package org.EventSource.core;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.time.Instant;
import java.util.Queue;
import java.util.UUID;

public interface AggregateRoot<S extends State> {
    void process(Command command) throws Exception;
    void apply(DomainEvent domainEvent) throws Exception;
    void applyFromEvents(DomainEvent domainEvent) throws Exception;
    void applyFromEvents(Snapshot snapshot, Queue<DomainEvent> domainEventQueue, Class<S> clazz) throws Exception;
    S state();
    Queue<DomainEvent> eventQueue();
    Long version();
    UUID id();
    Instant createdAt();
    Instant updatedAt();

}

package org.EventSource.core;
@FunctionalInterface
public interface AggregateEventHandler<S extends State,E extends DomainEvent> {
    S  onEvent(S state,E event) throws Exception;
}

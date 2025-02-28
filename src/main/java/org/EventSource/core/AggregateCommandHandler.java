package org.EventSource.core;

@FunctionalInterface
public interface AggregateCommandHandler<S extends State, C extends Command> {
    DomainEvent onCommand(S state, C command);
}

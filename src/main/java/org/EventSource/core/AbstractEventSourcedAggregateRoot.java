package org.EventSource.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.Instant;
import java.util.*;

public abstract class AbstractEventSourcedAggregateRoot<S extends State> implements AggregateRoot<S>{
    protected final Queue<DomainEvent> domainEventQueue;
    protected final Map<String, AggregateCommandHandler<S, ? extends Command>> commandHandlers;
    protected final Map<String, AggregateEventHandler<S, ? extends DomainEvent>> eventHandlers;
    private S state;
    private Long version;

    public AbstractEventSourcedAggregateRoot() {
        this.domainEventQueue = new LinkedList<>();
        this.commandHandlers = this.commandHandlers();
        this.eventHandlers   = this.eventHandlers();
        this.version = 0L;

    }

    @Override
    public void process(Command command) throws Exception {
        if(!this.commandHandlers.containsKey(command.getClass().getSimpleName())) {
            throw new RuntimeException(command.getClass().getSimpleName() + " command not found !");
        }
        AggregateCommandHandler<S, Command> commandHandler = (AggregateCommandHandler<S, Command>) this.commandHandlers.get(command.getClass().getSimpleName());
        apply(commandHandler.onCommand(this.state, command));
    }

    @Override
    public void apply(DomainEvent domainEvent) throws Exception {
        if(!this.eventHandlers.containsKey(domainEvent.getClass().getSimpleName())){
            throw new RuntimeException(domainEvent.getClass().getSimpleName() + " domainEvent not found !");
        }
        var eventHandler = (AggregateEventHandler<S, DomainEvent>) this.eventHandlers.get(domainEvent.getClass().getSimpleName());
        this.domainEventQueue.add(domainEvent);
        this.state = eventHandler.onEvent(this.state, domainEvent);
    }

    protected abstract Map<String, AggregateCommandHandler<S, ? extends Command>> commandHandlers();
    protected abstract Map<String, AggregateEventHandler<S, ? extends DomainEvent>> eventHandlers();


    @Override
    public void applyFromEvents(DomainEvent domainEvent) throws Exception {
        if(!this.eventHandlers.containsKey(domainEvent.getClass().getSimpleName())){
            throw new RuntimeException(domainEvent.getClass().getSimpleName() + " domainEvent not found !");
        }
        this.incrementVersion();
        var eventHandler = (AggregateEventHandler<S, DomainEvent>) this.eventHandlers.get(domainEvent.getClass().getSimpleName());
        this.state = eventHandler.onEvent(this.state, domainEvent);
    }
    @Override
    public void applyFromEvents(Snapshot snapshot, Queue<DomainEvent> domainEventQueue, Class<S> clazz) throws Exception {
        var mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        state = mapper.readValue(snapshot.aggregateState(), clazz);
        version = snapshot.version();
        while(!domainEventQueue.isEmpty()) {
            applyFromEvents(domainEventQueue.poll());
        }
    }
    private void incrementVersion() {
        version += 1L;
    }
    @Override
    public S state(){return this.state;}

    @Override
    public  Long version() {
        return this.version;
    }

    @Override
    public UUID id() {
        return this.state.id();
    }

    @Override
    public Instant createdAt(){
        return this.state.createdAt();
    }

    @Override
    public Instant updatedAt(){
        return this.state.updatedAt();
    }

    @Override
    public Queue<DomainEvent> eventQueue() {return this.domainEventQueue;}
}

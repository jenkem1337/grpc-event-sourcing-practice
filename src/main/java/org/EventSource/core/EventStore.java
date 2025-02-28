package org.EventSource.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.EventSource.example.Application.Snapshot;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public interface EventStore<A extends AggregateRoot<?>, ID> {
    Queue<DomainEvent> getEventsFor(ID id) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, SQLException, ClassNotFoundException, JsonProcessingException;
    org.EventSource.core.Snapshot getSnapshotFor(ID id) throws SQLException;
    Queue<DomainEvent> getEventsFrom(Long version, ID id) throws SQLException, ClassNotFoundException, JsonProcessingException;
    void saveChanges(A aggregate) throws SQLException, JsonProcessingException, OptimisticConcurrencyException;
    void saveSnapshot(List<Snapshot> snapshots, Class<A> clazz) throws SQLException;
    Map<String, DomainEventWrapper> getSnapshotNeededAggregateEvents(Long threshold, Class<A> clazz) throws SQLException, ClassNotFoundException, JsonProcessingException;
}

package org.EventSource.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.EventSource.example.Application.Snapshot;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

public class EventStoreImpl<A extends AggregateRoot<?>, ID extends UUID> implements EventStore<A, ID>{
    private final DataSource dataSource;

    public EventStoreImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Queue<DomainEvent> getEventsFor(ID id) throws SQLException, ClassNotFoundException, JsonProcessingException {
        return this.dataSource.findEventsByAggregateId(id);
    }
    @Override
    public org.EventSource.core.Snapshot getSnapshotFor(ID id) throws SQLException {
        return dataSource.findAggregateLatestStateFromSnapshot(id);
    }

    @Override
    public Queue<DomainEvent> getEventsFrom(Long version, ID id) throws SQLException, ClassNotFoundException, JsonProcessingException {
        return dataSource.findEventsByAggregateIdAndFromLatestSnapshotVersion(version, id);
    }

    @Override
    public void saveChanges(A aggregate) throws SQLException, JsonProcessingException, OptimisticConcurrencyException {
        long latestVersion = this.dataSource.getLatestVersion(aggregate.id());
        if(aggregate.version() != latestVersion) {
            throw new OptimisticConcurrencyException("Aggregate state has changed !");
        }
        if(latestVersion == 0L) {
            this.dataSource.insertAggregate(aggregate.id(), aggregate.getClass().getName());
            latestVersion = this.dataSource.saveEvents(aggregate.id(), aggregate.eventQueue(), latestVersion);
            this.dataSource.updateAggregateVersion(aggregate.id(), latestVersion);
            return;
        }
        latestVersion = this.dataSource.saveEvents(aggregate.id(), aggregate.eventQueue(), latestVersion);
        this.dataSource.updateAggregateVersion(aggregate.id(), latestVersion);
    }

    @Override
    public void saveSnapshot(List<Snapshot> snapshots, Class<A> clazz) throws SQLException {
        dataSource.saveSnapshot(snapshots, clazz);
    }

    @Override
    public Map<String, DomainEventWrapper> getSnapshotNeededAggregateEvents(Long threshold, Class<A> clazz) throws SQLException, ClassNotFoundException, JsonProcessingException {
        return dataSource.queryAggregatesNeedingSnapshot(threshold, clazz.getName());
    }
}

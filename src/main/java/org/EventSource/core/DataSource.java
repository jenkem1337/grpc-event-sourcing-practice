package org.EventSource.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.EventSource.example.Application.Snapshot;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

public interface DataSource {
    long getLatestVersion(UUID aggregateUUID) throws SQLException;
    long saveEvents(UUID aggregateUuid, Queue<DomainEvent> domainEventList, Long version ) throws SQLException, JsonProcessingException, OptimisticConcurrencyException;
    void updateAggregateVersion(UUID aggregateUuid, Long latestVersion) throws SQLException;
    void insertAggregate(UUID uuid, String aggregateType) throws SQLException;
    Queue<DomainEvent> findEventsByAggregateId(UUID id) throws SQLException, ClassNotFoundException, JsonProcessingException;
    Queue<DomainEvent> findEventsByAggregateIdAndFromLatestSnapshotVersion(Long version, UUID id) throws SQLException, ClassNotFoundException, JsonProcessingException;
    Map<String, DomainEventWrapper> queryAggregatesNeedingSnapshot(Long threshold, String type) throws SQLException, ClassNotFoundException, JsonProcessingException;
    void saveSnapshot(List<Snapshot> snapshot, Class<?> clazz) throws SQLException;
    org.EventSource.core.Snapshot findAggregateLatestStateFromSnapshot(UUID aggregateId) throws SQLException;
}

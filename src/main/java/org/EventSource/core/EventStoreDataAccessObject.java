package org.EventSource.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.EventSource.example.Application.Snapshot;

import java.sql.*;
import java.util.*;

public class EventStoreDataAccessObject implements DataSource {
    private final TransactionManager transactionManager;
    public EventStoreDataAccessObject(TransactionManager transactionManager) throws SQLException {
        this.transactionManager = transactionManager;
    }
    @Override
    public long getLatestVersion(UUID aggregateUUID) throws SQLException {
        Connection connection = transactionManager.getConnection();
        try(
                PreparedStatement stmt = connection.prepareStatement("SELECT version FROM entities WHERE id = ?")) {
            stmt.setObject(1,aggregateUUID);
            ResultSet rs = stmt.executeQuery();
            long version = 0L;
            if(rs.next()) {
                version = rs.getLong(1);
            }
            return version;
        } finally {
            transactionManager.closeConnection(connection);
        }
    }

    @Override
    public long saveEvents(UUID aggregateUuid, Queue<DomainEvent> domainEventList, Long version) throws SQLException, JsonProcessingException, OptimisticConcurrencyException {
        Connection connection = transactionManager.getConnection();
        try(
            PreparedStatement stmt = connection.prepareStatement("INSERT INTO events (id, aggregate_id, type, data, version) VALUES (?, ?, ?, ?::jsonb, ? ) ON CONFLICT DO NOTHING ")){
            while (!domainEventList.isEmpty()) {
                version += 1;
                DomainEvent domainEvent = domainEventList.poll();
                stmt.setObject(1, UUID.randomUUID());
                stmt.setObject(2, aggregateUuid);
                stmt.setString(3, domainEvent.getClass().getName());
                stmt.setString(4, domainEvent.toJson());
                stmt.setLong(5, version);

                stmt.addBatch();
            }
            int[] isInserted = stmt.executeBatch();
            for (int j : isInserted) {
                if (j == 0) {
                    throw new OptimisticConcurrencyException("The same event has been attempted to be inserted");
                }
            }
        } finally {
            transactionManager.closeConnection(connection);
        }
        return version;
    }
    @Override
    public void insertAggregate(UUID uuid, String aggregateType) throws SQLException {
        Connection connection = transactionManager.getConnection();
        try(
                PreparedStatement stmt = connection.prepareStatement("INSERT INTO entities (id, type, version) VALUES (?, ?, ?)")){
            stmt.setObject(1,uuid);
            stmt.setString(2,aggregateType);
            stmt.setLong(3, 0L);

            stmt.executeUpdate();
        }finally {
            transactionManager.closeConnection(connection);
        }

    }
    @Override
    public void updateAggregateVersion(UUID aggregateUuid, Long latestVersion) throws SQLException {
        Connection connection = transactionManager.getConnection();
        try(
                PreparedStatement stmt = connection.prepareStatement("UPDATE entities SET version = ? WHERE id = ?")){
            stmt.setLong(1, latestVersion);
            stmt.setObject(2, aggregateUuid);
            stmt.executeUpdate();
        }finally {
            transactionManager.closeConnection(connection);
        }
    }
    @Override
    public Queue<DomainEvent> findEventsByAggregateId(UUID id) throws SQLException, ClassNotFoundException, JsonProcessingException {
        Queue<DomainEvent> domainEventQueue = new LinkedList<>();
        ObjectMapper mapper = new ObjectMapper();
        Connection connection = transactionManager.getConnection();
        try(
                PreparedStatement stmt = connection.prepareStatement("SELECT type, data FROM events WHERE aggregate_id = ? ORDER BY version ASC")){
            stmt.setObject(1, id);
            ResultSet rs = stmt.executeQuery();

            while(rs.next()) {
                String type = rs.getString(1);
                String json = rs.getString(2);
                DomainEvent domainEvent = (DomainEvent) mapper.readValue(json, Class.forName(type));
                domainEventQueue.add(domainEvent);
            }
        }finally {
            transactionManager.closeConnection(connection);
        }
        return domainEventQueue;
    }
    @Override
    public Queue<DomainEvent> findEventsByAggregateIdAndFromLatestSnapshotVersion(Long version, UUID id) throws SQLException, ClassNotFoundException, JsonProcessingException {
        Queue<DomainEvent> domainEventQueue = new LinkedList<>();
        ObjectMapper mapper = new ObjectMapper();
        Connection connection = transactionManager.getConnection();
        try(
                PreparedStatement stmt = connection.prepareStatement("SELECT type, data FROM events WHERE aggregate_id = ? AND version > ? ORDER BY version ASC")){
            stmt.setObject(1, id);
            stmt.setLong(2, version);
            ResultSet rs = stmt.executeQuery();

            while(rs.next()) {
                String type = rs.getString(1);
                String json = rs.getString(2);
                DomainEvent domainEvent = (DomainEvent) mapper.readValue(json, Class.forName(type));
                domainEventQueue.add(domainEvent);
            }
        }finally {
            transactionManager.closeConnection(connection);
        }
        return domainEventQueue;

    }
    @Override
    public Map<String, DomainEventWrapper> queryAggregatesNeedingSnapshot(Long threshold, String type) throws SQLException, ClassNotFoundException, JsonProcessingException {
        Map<String, DomainEventWrapper> eventMap = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        Connection connection = transactionManager.getConnection();
        try(
                PreparedStatement stmt = connection.prepareStatement(
                """
                        SELECT aggregate_id, type, data FROM events e
                        WHERE aggregate_id IN (SELECT DISTINCT en.id
                            FROM entities en
                            LEFT JOIN snapshots s ON en.id = s.aggregate_id
                            GROUP BY en.id
                            HAVING en.type = ? AND ( en.version - MAX(COALESCE(s.version, 0)) ) > ? )
                        ORDER BY e.version ASC
                   """)){
            stmt.setString(1, type);
            stmt.setLong(2, threshold);
            ResultSet rs = stmt.executeQuery();
            while(rs.next()) {

                String aggregateUuid = rs.getString(1);
                String eventType     = rs.getString(2);
                String eventJsonBody = rs.getString(3);

                DomainEvent domainEvent = (DomainEvent) mapper.readValue(eventJsonBody, Class.forName(eventType));

                if(!eventMap.containsKey(aggregateUuid)){
                    List<DomainEvent> domainEventList = new ArrayList<>();
                    domainEventList.add(domainEvent);
                    eventMap.put(aggregateUuid, new DomainEventWrapper(domainEventList, aggregateUuid, null, null));
                } else {
                    var domainEventWrapper = eventMap.get(aggregateUuid);
                    domainEventWrapper.domainEventList.add(domainEvent);
                    eventMap.put(aggregateUuid, domainEventWrapper);
                }
            }
        }finally {
            transactionManager.closeConnection(connection);
        }
        return eventMap;
    }

    @Override
    public void saveSnapshot(List<org.EventSource.example.Application.Snapshot> snapshots, Class<?> clazz) throws SQLException {
        Connection connection = transactionManager.getConnection();
        try(
                PreparedStatement stmt = connection.prepareStatement("""
                INSERT INTO snapshots (id, aggregate_id, aggregate_type, data, version) VALUES (?, ?, ?, ?::jsonb, ?)
                ON CONFLICT (aggregate_id, version) DO NOTHING
                """)){
            for(Snapshot snapshot : snapshots) {
                stmt.setObject(1, UUID.randomUUID());
                stmt.setObject(2, UUID.fromString(snapshot.getAggregateId()));
                stmt.setString(3, clazz.getName());
                stmt.setString(4, snapshot.getAggregateLatestState());
                stmt.setLong(5, snapshot.getVersion());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }finally {
            transactionManager.closeConnection(connection);
        }
    }

    public org.EventSource.core.Snapshot findAggregateLatestStateFromSnapshot(UUID aggregateId) throws SQLException {
        Connection connection = transactionManager.getConnection();
        try(PreparedStatement stmt = connection.prepareStatement("SELECT aggregate_id, data, version FROM snapshots WHERE aggregate_id = ? ORDER BY version DESC LIMIT 1")){
            stmt.setObject(1,aggregateId);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()) {
                return new org.EventSource.core.Snapshot(UUID.fromString(rs.getString(1)), rs.getString(2), rs.getLong(3));
            }
            return null;
        } finally {
            transactionManager.closeConnection(connection);
        }
    }

}

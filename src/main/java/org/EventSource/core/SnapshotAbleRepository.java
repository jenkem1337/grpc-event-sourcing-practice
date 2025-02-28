package org.EventSource.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.EventSource.example.Application.Snapshot;

import java.sql.SQLException;
import java.util.List;

public interface SnapshotAbleRepository<A extends AbstractEventSourcedAggregateRoot<?>> extends Repository<A>{
    void saveSnapshot(List<Snapshot> snapshot) throws SQLException;
    List<SnapshotVersionAndAggregateRootWrapper<A>> getSnapshotNeededAggregate(Long threshold) throws Exception;
}

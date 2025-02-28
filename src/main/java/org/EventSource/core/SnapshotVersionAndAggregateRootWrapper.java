package org.EventSource.core;

public class SnapshotVersionAndAggregateRootWrapper<A extends AbstractEventSourcedAggregateRoot<?>> {
    private final Long latestSnapshotVersion;
    private final  A aggregateRoot;

    public SnapshotVersionAndAggregateRootWrapper(Long latestSnapshotVersion, A aggregateRoot) {
        this.latestSnapshotVersion = latestSnapshotVersion;
        this.aggregateRoot = aggregateRoot;
    }

    public Long getLatestSnapshotVersion() {
        return latestSnapshotVersion;
    }

    public A getAggregateRoot() {return aggregateRoot;}
}

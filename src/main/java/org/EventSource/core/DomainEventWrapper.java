package org.EventSource.core;
import java.util.List;
public class DomainEventWrapper {
    public final List<DomainEvent> domainEventList;
    public final String aggregateUuid ;
    public final String aggregateType ;
    public final Long snapshotVersion;

    public DomainEventWrapper(List<DomainEvent> domainEventList, String aggregateUuid, String aggregateType, Long snapshotVersion) {
        this.domainEventList = domainEventList;
        this.aggregateUuid = aggregateUuid;
        this.aggregateType = aggregateType;
        this.snapshotVersion = snapshotVersion;
    }
}

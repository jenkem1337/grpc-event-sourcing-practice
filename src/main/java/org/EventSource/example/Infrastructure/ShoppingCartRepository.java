package org.EventSource.example.Infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.EventSource.core.*;
import org.EventSource.example.Application.Snapshot;
import org.EventSource.example.Domain.ShoppingCart.ShoppingCartAggregate;
import org.EventSource.example.Domain.ShoppingCart.ShoppingCartState;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

public class ShoppingCartRepository implements SnapshotAbleRepository<ShoppingCartAggregate> {
    private final EventStore<ShoppingCartAggregate, UUID> eventStore;
    public ShoppingCartRepository(EventStore<ShoppingCartAggregate, UUID> eventStore){
        this.eventStore = eventStore;
    }
    @Override
    public ShoppingCartAggregate findById(UUID uuid) throws Exception {
        org.EventSource.core.Snapshot snapshot = eventStore.getSnapshotFor(uuid);
        if(snapshot != null) {
            System.out.println("snapshottan birleştiriliyor");
            Queue<DomainEvent> events = eventStore.getEventsFrom(snapshot.version(), snapshot.aggregateId());
            ShoppingCartAggregate aggregate = ShoppingCartAggregate.class.getConstructor().newInstance();
            aggregate.applyFromEvents(snapshot, events, ShoppingCartState.class);
            return aggregate;
        }
        Queue<DomainEvent> events = eventStore.getEventsFor(uuid);
        ShoppingCartAggregate aggregate = ShoppingCartAggregate.class.getConstructor().newInstance();

        while(!events.isEmpty()) {
            aggregate.applyFromEvents(events.poll());
        }
        return aggregate;
    }

    @Override
    public void saveChanges(ShoppingCartAggregate aggregate) throws OptimisticConcurrencyException, SQLException, JsonProcessingException {
        eventStore.saveChanges(aggregate);
    }

    @Override
    public void saveSnapshot(List<Snapshot> snapshots) throws SQLException {
        eventStore.saveSnapshot(snapshots, ShoppingCartAggregate.class);
    }

    @Override
    public List<SnapshotVersionAndAggregateRootWrapper<ShoppingCartAggregate>> getSnapshotNeededAggregate(Long threshold) throws Exception {
        var domainEvents = eventStore.getSnapshotNeededAggregateEvents(threshold, ShoppingCartAggregate.class);

        List<SnapshotVersionAndAggregateRootWrapper<ShoppingCartAggregate>> aggregateList = new ArrayList<>();

        for(DomainEventWrapper domainEventWrapper : domainEvents.values()) {

            ShoppingCartAggregate shoppingCart = ShoppingCartAggregate.class.getConstructor().newInstance();

            for(DomainEvent domainEvent : domainEventWrapper.domainEventList) {
                shoppingCart.applyFromEvents(domainEvent);
            }

            aggregateList.add(new SnapshotVersionAndAggregateRootWrapper<>(null, shoppingCart));
        }
        return aggregateList;
    }
}

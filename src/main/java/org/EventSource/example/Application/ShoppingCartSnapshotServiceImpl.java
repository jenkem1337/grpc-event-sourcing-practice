package org.EventSource.example.Application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.EventSource.core.SnapshotAbleRepository;
import org.EventSource.core.SnapshotVersionAndAggregateRootWrapper;
import org.EventSource.core.TransactionManager;
import org.EventSource.example.Domain.ShoppingCart.ShoppingCartAggregate;

import java.util.ArrayList;
import java.util.List;

public class ShoppingCartSnapshotServiceImpl extends ShoppingCartSnapshotServiceGrpc.ShoppingCartSnapshotServiceImplBase{
    private final TransactionManager transactionManager;
    private final SnapshotAbleRepository<ShoppingCartAggregate> repository;

    public ShoppingCartSnapshotServiceImpl(TransactionManager transactionManager, SnapshotAbleRepository<ShoppingCartAggregate> repository) {
        this.transactionManager = transactionManager;
        this.repository = repository;
    }

    @Override
    public void getLatestAggregateStateForSnapshot(LatestAggregateStateRequest request, StreamObserver<LatestAggregateStateResponse> streamObserver) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());

            List<SnapshotVersionAndAggregateRootWrapper<ShoppingCartAggregate>> aggregateList = repository.getSnapshotNeededAggregate((long) request.getVersionThreshold());
            List<Snapshot> snapshotList = new ArrayList<>();

            for(SnapshotVersionAndAggregateRootWrapper<ShoppingCartAggregate> wrapper : aggregateList) {
                Snapshot snapshot = Snapshot.newBuilder()
                        .setAggregateId(wrapper.getAggregateRoot().id().toString())
                        .setAggregateLatestState(mapper.writeValueAsString(wrapper.getAggregateRoot().state()))
                        .setVersion(wrapper.getAggregateRoot().version())
                        .build();
                snapshotList.add(snapshot);
            }

            LatestAggregateStateResponse response = LatestAggregateStateResponse.newBuilder()
                    .addAllSnapshots(snapshotList)
                    .build();
            streamObserver.onNext(response);
            streamObserver.onCompleted();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            streamObserver.onError(e);
        }
    }

    @Override
    public void saveSnapshot(SaveSnapshotRequest request, StreamObserver<Empty> streamObserver) {
        try{
            transactionManager.beginTransaction();
            repository.saveSnapshot(request.getSnapshotsList());
            transactionManager.commit();
            streamObserver.onNext(Empty.getDefaultInstance());
            streamObserver.onCompleted();
        } catch (Exception e) {
            transactionManager.rollback();
            System.out.println(e.getMessage());
            streamObserver.onError(e);

        }
    }
}

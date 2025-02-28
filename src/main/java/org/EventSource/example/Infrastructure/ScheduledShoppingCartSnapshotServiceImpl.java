package org.EventSource.example.Infrastructure;

import io.etcd.jetcd.*;
import io.etcd.jetcd.lease.LeaseGrantResponse;
import org.EventSource.core.ScheduledSnapshotService;
import org.EventSource.example.Application.LatestAggregateStateRequest;
import org.EventSource.example.Application.SaveSnapshotRequest;
import org.EventSource.example.Application.ShoppingCartSnapshotServiceGrpc;
import org.EventSource.example.Application.Snapshot;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScheduledShoppingCartSnapshotServiceImpl extends ScheduledSnapshotService {
    private final ShoppingCartSnapshotServiceGrpc.ShoppingCartSnapshotServiceBlockingStub stub;
    private static final String LEADER_KEY = "/leader";
    private static final long LEASE_TTL = 10; // 10 saniye
    private  String nodeId;
    private  Lease leaseClient;
    private  Lock lockClient;
    private long leaseId;
    private boolean isLeader;
    private  ScheduledExecutorService leaderMaintainerExecutor;

    public ScheduledShoppingCartSnapshotServiceImpl(String nodeId, ShoppingCartSnapshotServiceGrpc.ShoppingCartSnapshotServiceBlockingStub stub, Client etcdClient, ScheduledExecutorService scheduledExecutorService) {
        super(etcdClient, scheduledExecutorService);
        this.nodeId = nodeId;
        this.stub = stub;
        this.leaseClient = this.etcdClient.getLeaseClient();
        this.lockClient = this.etcdClient.getLockClient();
        this.leaderMaintainerExecutor = Executors.newSingleThreadScheduledExecutor();

    }
    public void startElection() {
        System.out.println("Node " + nodeId + " starting...");
        leaderMaintainerExecutor.scheduleAtFixedRate(this::attemptToBeLeader, 0, 5, TimeUnit.SECONDS);

    }
    private void attemptToBeLeader() {
        try {
            if (isLeader) {
                renewLease();
            } else {
                tryToAcquireLeadership();
            }
        } catch (Exception e) {
            System.err.println("Leader election error: " + e.getMessage());
            isLeader = false;
        }
    }

    private void tryToAcquireLeadership() throws ExecutionException, InterruptedException {
        try {
            LeaseGrantResponse leaseResponse = leaseClient.grant(LEASE_TTL).get();
            leaseId = leaseResponse.getID();

            ByteSequence key = ByteSequence.from(LEADER_KEY, StandardCharsets.UTF_8);
            var lockResponse = lockClient.lock(key, leaseId).get();
            if(lockResponse != null) {
                isLeader = true;
                System.out.println(nodeId + " become leader !");
            }
        } catch (Exception e) {
            leaseClient.revoke(leaseId).get();
            System.out.println(nodeId + " not become leader.");
        }
    }
    private void renewLease() throws ExecutionException, InterruptedException {
        Lease leaseClient = etcdClient.getLeaseClient();

        leaseClient.keepAliveOnce(leaseId).get();
        System.out.println("Node " + nodeId + " continue to leadership. Lease has been renewed.");

    }

    @Override
    protected void doSnapshot(int threshold) throws Exception {
        if(isLeader) {
            LatestAggregateStateRequest request = LatestAggregateStateRequest.newBuilder()
                    .setVersionThreshold(threshold)
                    .build();

            var response = stub.getLatestAggregateStateForSnapshot(request);

            if(response.getSnapshotsList().isEmpty()) {
                System.out.println("No need to take snapshot");
                return;
            }
            var emptyResult = stub.saveSnapshot(SaveSnapshotRequest.newBuilder().addAllSnapshots(response.getSnapshotsList()).build());
            System.out.println("All snapshots taken successfully :)");
            return;
        }
        System.out.println(nodeId + " is not leader, just relaxing :)");

    }
}

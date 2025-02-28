package org.EventSource.core;

import io.etcd.jetcd.Client;

import java.util.Arrays;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class ScheduledSnapshotService {
    protected final Client etcdClient;
    protected final ScheduledExecutorService scheduledExecutorService;

    protected ScheduledSnapshotService(Client etcdClient, ScheduledExecutorService scheduledExecutorService) {
        this.etcdClient = etcdClient;
        this.scheduledExecutorService = scheduledExecutorService;
    }
    public void submit(int threshold, Integer firstDelay, Integer interval, TimeUnit timeUnit) {
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                doSnapshot(threshold);
            } catch (Exception e) {
                System.err.println(Arrays.toString(e.getStackTrace()));
            }
        }, firstDelay, interval, timeUnit);

    }
    protected  abstract void doSnapshot(int threshold) throws Exception;

}

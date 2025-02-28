package org.EventSource.example;

import io.etcd.jetcd.Client;
import io.grpc.LoadBalancerRegistry;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;

import io.grpc.internal.PickFirstLoadBalancerProvider;
import io.grpc.netty.NettyServerBuilder;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.EventSource.core.*;
import org.EventSource.core.PostgresConnectionFactory;
import org.EventSource.core.ThreadLocalTransactionManager;
import org.EventSource.example.Application.ShoppingCartServiceImpl;
import org.EventSource.example.Application.ShoppingCartSnapshotServiceGrpc;
import org.EventSource.example.Application.ShoppingCartSnapshotServiceImpl;
import org.EventSource.example.Domain.ShoppingCart.ShoppingCartAggregate;
import org.EventSource.example.Infrastructure.ScheduledShoppingCartSnapshotServiceImpl;
import org.EventSource.example.Infrastructure.ShoppingCartRepository;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {


    public static void main(String[] args) throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException, SQLException, IOException, ClassNotFoundException, OptimisticConcurrencyException, InterruptedException {
        if(Objects.equals(args[0], "snapshot-client")) {
            LoadBalancerRegistry.getDefaultRegistry().register(new PickFirstLoadBalancerProvider());

            ManagedChannel channel = ManagedChannelBuilder.forAddress(System.getenv("BACKEND_HOST"),Integer.parseInt(System.getenv("BACKEND_PORT")))
                    .usePlaintext()
                    .build();
            Client client = Client.builder().target(System.getenv("ETCD_TARGET")).build();

            var snapshotService = new ScheduledShoppingCartSnapshotServiceImpl(UUID.randomUUID().toString(), ShoppingCartSnapshotServiceGrpc.newBlockingStub(channel), client, Executors.newSingleThreadScheduledExecutor());
            snapshotService.startElection();
            snapshotService.submit(
                    Integer.parseInt(System.getenv("SNAPSHOT_THRESHOLD")),
                    Integer.parseInt(System.getenv("SCHEDULER_INITIAL_DELAY")),
                    Integer.parseInt(System.getenv("SCHEDULER_INTERVAL")),
                    TimeUnit.SECONDS);
            return;
        }
        if(Objects.equals(args[0], "backend")) {
            var prop = new Properties();
            prop.setProperty("host", System.getenv("DB_HOST"));
            prop.setProperty("port", System.getenv("DB_PORT"));
            prop.setProperty("user", System.getenv("DB_USER"));
            prop.setProperty("password", System.getenv("DB_PASSWORD"));
            ConnectionFactory connectionFactory = new PostgresConnectionFactory(prop);
            var txm = new ThreadLocalTransactionManager(connectionFactory);
            var db = new EventStoreDataAccessObject(txm);
            var eventStore = new EventStoreImpl<ShoppingCartAggregate, UUID>(db);
            var repository = new ShoppingCartRepository(eventStore);
            int coreCount = Runtime.getRuntime().availableProcessors();

            EventLoopGroup bossGroup = new NioEventLoopGroup(1);
            EventLoopGroup workerGroup = new NioEventLoopGroup(coreCount);

            Server server = NettyServerBuilder.forPort(Integer.parseInt(System.getenv("APP_PORT")))
                    .bossEventLoopGroup(bossGroup)
                    .workerEventLoopGroup(workerGroup)
                    .channelType(NioServerSocketChannel.class)
                    .addService(new ShoppingCartServiceImpl(txm, repository))
                    .addService(new ShoppingCartSnapshotServiceImpl(txm, repository))
                    .build()
                    .start();
            System.out.println("Service has started on port 9090");
            server.awaitTermination();

        }

    }

}
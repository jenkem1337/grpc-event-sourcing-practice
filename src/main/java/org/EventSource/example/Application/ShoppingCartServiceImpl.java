package org.EventSource.example.Application;

import io.grpc.Metadata;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.StreamObserver;
import org.EventSource.core.Repository;
import org.EventSource.core.TransactionManager;
import org.EventSource.example.Domain.ShoppingCart.Command.AddAnItem;
import org.EventSource.example.Domain.ShoppingCart.Command.CompleteShopping;
import org.EventSource.example.Domain.ShoppingCart.Command.RemoveAnItem;
import org.EventSource.example.Domain.ShoppingCart.Command.RemoveItem;
import org.EventSource.example.Domain.ShoppingCart.Event.AnItemRemoved;
import org.EventSource.example.Domain.ShoppingCart.ShoppingCartAggregate;

import java.math.BigDecimal;
import java.util.UUID;

public class ShoppingCartServiceImpl extends ShoppingCartServiceGrpc.ShoppingCartServiceImplBase{
    private final TransactionManager transactionManager;
    private final Repository<ShoppingCartAggregate> repository;

    public  ShoppingCartServiceImpl(TransactionManager txm, Repository<ShoppingCartAggregate> repository) {
        this.transactionManager = txm;
        this.repository = repository;
    }
    @Override
    public void createCart(CreateShoppingCartRequest request, StreamObserver<ShoppingCartCreatedResponse> streamObserver) {
        try {
            transactionManager.beginTransaction();
            var shoppingCart = ShoppingCartAggregate.createWithUserId(UUID.fromString(request.getUserId()));
            repository.saveChanges(shoppingCart);
            ShoppingCartCreatedResponse response = ShoppingCartCreatedResponse.newBuilder()
                    .setUuid(shoppingCart.id().toString())
                    .build();
            transactionManager.commit();
            streamObserver.onNext(response);
            streamObserver.onCompleted();
        } catch (Exception e) {
            transactionManager.rollback();
            System.out.println(e.getMessage());
            streamObserver.onError(e);
        }
    }

    @Override
    public void addAnItem(AddAnItemRequest request, StreamObserver<ItemAddedResponse> streamObserver) {
        var unit = BigDecimal.valueOf(request.getMoneyUnits());
        var nanos = BigDecimal.valueOf(request.getMoneyNanos(), 9);
        var result = unit.add(nanos);

        try {
            transactionManager.beginTransaction();
            ShoppingCartAggregate aggregate = repository.findById(UUID.fromString(request.getAggregateId()));
            aggregate.process(AddAnItem.create(UUID.fromString(request.getAggregateId()), request.getCartItemId(), UUID.fromString(request.getProductId()), request.getProductName(), result));
            repository.saveChanges(aggregate);
            transactionManager.commit();
            var response = ItemAddedResponse.newBuilder()
                    .setAggregateId(aggregate.id().toString())
                    .build();
            streamObserver.onNext(response);
            streamObserver.onCompleted();

        } catch (Exception e) {
            transactionManager.rollback();
            System.out.println(e.getMessage());
            streamObserver.onError(e);
        }
    }

    @Override
    public void removeAnItem(RemoveAnItemRequest request, StreamObserver<AnItemRemovedResponse> streamObserver) {
        try {
            transactionManager.beginTransaction();
            ShoppingCartAggregate aggregate = repository.findById(UUID.fromString(request.getCartId()));
            aggregate.process(new RemoveAnItem(UUID.fromString(request.getCartItemId())));
            repository.saveChanges(aggregate);
            transactionManager.commit();
            var response = AnItemRemovedResponse.newBuilder()
                    .setAggregateId(aggregate.id().toString())
                    .build();
            streamObserver.onNext(response);
            streamObserver.onCompleted();

        } catch (Exception e) {
            transactionManager.rollback();
            System.out.println(e.getMessage());
            streamObserver.onError(e);
        }

    }

    @Override
    public void removeItem(RemoveItemRequest request, StreamObserver<ItemRemovedResponse> streamObserver) {
        try {
            transactionManager.beginTransaction();
            ShoppingCartAggregate aggregate = repository.findById(UUID.fromString(request.getCartId()));
            aggregate.process(new RemoveItem(UUID.fromString(request.getCartItemId())));
            repository.saveChanges(aggregate);
            transactionManager.commit();
            var response = ItemRemovedResponse.newBuilder()
                    .setAggregateId(aggregate.id().toString())
                    .build();
            streamObserver.onNext(response);
            streamObserver.onCompleted();

        } catch (Exception e) {
            transactionManager.rollback();
            System.out.println(e.getMessage());
            streamObserver.onError(e);
        }

    }

    public void completeShopping(CompleteShoppingRequest request, StreamObserver<ShoppingCompletedResponse> streamObserver) {
        try {
            transactionManager.beginTransaction();
            ShoppingCartAggregate aggregate = repository.findById(UUID.fromString(request.getCartId()));
            aggregate.process(new CompleteShopping(UUID.fromString(request.getCartId())));
            repository.saveChanges(aggregate);
            transactionManager.commit();
            var response = ShoppingCompletedResponse.newBuilder()
                    .setCartId(aggregate.id().toString())
                    .build();
            streamObserver.onNext(response);
            streamObserver.onCompleted();

        } catch (Exception e) {
            transactionManager.rollback();
            System.out.println(e.getMessage());
            streamObserver.onError(e);
        }

    }

}

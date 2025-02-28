package org.EventSource.example.Domain.ShoppingCart;

import org.EventSource.core.*;
import org.EventSource.example.Domain.ShoppingCart.Command.*;
import org.EventSource.example.Domain.ShoppingCart.Event.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShoppingCartAggregate extends AbstractEventSourcedAggregateRoot<ShoppingCartState> {
    public ShoppingCartAggregate() {
        super();
    }
    private ShoppingCartAggregate(CreateShoppingCart createShoppingCart) throws Exception {
        super();
        apply(new ShoppingCartCreated(createShoppingCart.shoppingCartId(), createShoppingCart.userId()));
    }
    @Override
    protected Map<String, AggregateCommandHandler<ShoppingCartState, ? extends Command>> commandHandlers() {
        Map<String, AggregateCommandHandler<ShoppingCartState, ? extends Command>> commandHandlerMap = new HashMap<>();
        commandHandlerMap.put("AddAnItem", addAnItem());
        commandHandlerMap.put("RemoveAnItem" , removeAnItem());
        commandHandlerMap.put("RemoveItem", removeItem());
        commandHandlerMap.put("CompleteShopping", completeShopping());
        return commandHandlerMap;
    }

    @Override
    protected Map<String, AggregateEventHandler<ShoppingCartState, ? extends DomainEvent>> eventHandlers() {
        Map<String, AggregateEventHandler<ShoppingCartState, ? extends DomainEvent>> eventHandlerMap = new HashMap<>();
        eventHandlerMap.put("ShoppingCartCreated", onCartCreated());
        eventHandlerMap.put("CartItemQuantityIncremented", onCartItemQuantityIncreased());
        eventHandlerMap.put("CartItemCreated", onCartItemCreated());
        eventHandlerMap.put("AnItemRemoved", onAnItemRemoved());
        eventHandlerMap.put("CartItemRemoved", onCartItemRemoved());
        eventHandlerMap.put("ShoppingCompleted", onShoppingCompleted());
        return eventHandlerMap;
    }
    private AggregateEventHandler<ShoppingCartState, ShoppingCartCreated> onCartCreated() {
        return (state, event) -> new ShoppingCartState(event.cartId(), event.userId(), new HashMap<>(),  CartState.CREATED, Instant.now(), Instant.now());
    }
    private AggregateCommandHandler<ShoppingCartState, AddAnItem> addAnItem() {
        return (state, command) -> {
            if(state.isItemAvailableInCart(UUID.fromString(command.cartItemId()))) {
                return new CartItemQuantityIncremented(UUID.fromString(command.cartItemId()));
            }
            return new CartItemCreated(command.cartItemId(), command.productId(), command.productName(), command.price());
        };
    }
    private AggregateEventHandler<ShoppingCartState, CartItemCreated> onCartItemCreated() {
        return (state, event) -> state.addNewItem(UUID.fromString(event.cartItemId()), event.productId(), event.productName(), event.price());
    }
    private AggregateEventHandler<ShoppingCartState, CartItemQuantityIncremented> onCartItemQuantityIncreased() {
        return (state, event) -> state.addItem(event.cartItemId());
    }

    private AggregateCommandHandler<ShoppingCartState, RemoveAnItem> removeAnItem() {
        return (state, command) -> new AnItemRemoved(command.cartItemId());
    }
    private AggregateEventHandler<ShoppingCartState, AnItemRemoved> onAnItemRemoved() {
        return (state, event) -> state.removeAnItem(event.cartItemId());
    }

    private AggregateCommandHandler<ShoppingCartState, RemoveItem> removeItem() {
        return (state, command) -> new CartItemRemoved(command.cartItemId());
    }
    private AggregateEventHandler<ShoppingCartState, CartItemRemoved> onCartItemRemoved() {
        return (state, event) -> state.removeItem(event.cartItemId());
    }

    private AggregateCommandHandler<ShoppingCartState, CompleteShopping> completeShopping() {
        return (state, command) -> new ShoppingCompleted(command.cartId());
    }
    private AggregateEventHandler<ShoppingCartState, ShoppingCompleted> onShoppingCompleted() {
        return (state, event) -> state.complete();
    }

    public static ShoppingCartAggregate createWithUserId(UUID userId) throws Exception {
        return new ShoppingCartAggregate(new CreateShoppingCart(UUID.randomUUID(), userId));
    }
}

package org.EventSource.example.Domain.ShoppingCart;

import org.EventSource.core.State;
import org.EventSource.example.Domain.ShoppingCart.ValueObject.BasePrice;
import org.EventSource.example.Domain.ShoppingCart.ValueObject.Quantity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record ShoppingCartState(
        UUID id,
        UUID userUuid,
        Map<UUID, CartItem> items,
        CartState state,
        Instant createdAt,
        Instant updatedAt) implements State {

    public ShoppingCartState addNewItem(UUID newCartItemId,UUID productId, String productName, BigDecimal price) throws Exception {
        isCompleted();
        items.put(newCartItemId, CartItem.create(newCartItemId, productId, productName, BasePrice.create(price), Quantity.create(1)));
        return new ShoppingCartState(id, userUuid, items, state, createdAt, Instant.now());
    }

    public ShoppingCartState addItem(UUID cartItem) throws Exception {
        isCompleted();
        var item = items.get(cartItem);
        item.incrementQuantity();
        items.put(cartItem, item);
        return new ShoppingCartState(id, userUuid, items, state, createdAt, Instant.now());
    }
    public boolean isItemAvailableInCart(UUID cartItemId) {
        return items.containsKey(cartItemId);
    }

    public ShoppingCartState removeAnItem(UUID cartItemId) throws Exception {
        isCompleted();
        var item = items.get(cartItemId);
        if(item == null) {
            throw new Exception("Item not found");
        }
        item.decrementQuantity();
        if(item.getQuantity().isQuantityEqualZero()) {
            items.remove(cartItemId);
        } else {
            items.put(cartItemId, item);
        }
        return new ShoppingCartState(id, userUuid, items, state, createdAt, Instant.now());
    }

    public ShoppingCartState removeItem(UUID cartItemId) throws Exception {
        isCompleted();
        if(!items.containsKey(cartItemId)) {
            throw new Exception("Item not found");
        }
        items.remove(cartItemId);
        return new ShoppingCartState(id, userUuid, items, state, createdAt, Instant.now());
    }

    public ShoppingCartState complete() {
        return new ShoppingCartState(id ,userUuid, items, CartState.COMPLETED, createdAt,Instant.now());
    }
    private void isCompleted() throws Exception {
        if(state == CartState.COMPLETED) {
            throw new Exception("Shopping completed, you can not add or remove item form cart !");
        }
    }
}

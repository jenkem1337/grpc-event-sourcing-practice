package org.EventSource.example.Domain.ShoppingCart.Command;

import org.EventSource.core.Command;

import java.math.BigDecimal;
import java.util.UUID;

public record AddAnItem(UUID cartId,  String cartItemId, UUID productId, String productName, BigDecimal price) implements Command {
    public static AddAnItem create (UUID cartId,  String cartItemId, UUID productId, String productName, BigDecimal price) {
        if(cartItemId.isBlank()) {
            cartItemId = UUID.randomUUID().toString();
        }
        return new AddAnItem(cartId,  cartItemId, productId, productName, price);
    }
}

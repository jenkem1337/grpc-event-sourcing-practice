package org.EventSource.example.Domain.ShoppingCart.Command;

import org.EventSource.core.Command;

import java.util.UUID;

public record CompleteShopping(UUID cartId) implements Command {
}

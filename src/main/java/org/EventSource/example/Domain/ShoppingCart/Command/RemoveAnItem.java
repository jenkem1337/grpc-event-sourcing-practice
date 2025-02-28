package org.EventSource.example.Domain.ShoppingCart.Command;

import org.EventSource.core.Command;

import java.util.UUID;

public record RemoveAnItem(UUID cartItemId) implements Command {
}

package org.EventSource.example.Domain.ShoppingCart.Command;

import org.EventSource.core.Command;
import org.EventSource.core.DomainEvent;

import java.util.UUID;

public record RemoveItem(UUID cartItemId) implements Command {
}

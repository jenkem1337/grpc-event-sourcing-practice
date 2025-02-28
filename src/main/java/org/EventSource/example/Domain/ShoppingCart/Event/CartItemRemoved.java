package org.EventSource.example.Domain.ShoppingCart.Event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.EventSource.core.DomainEvent;

import java.util.UUID;

public record CartItemRemoved(UUID cartItemId) implements DomainEvent {


    public String toJson() throws JsonProcessingException {
        var mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }

}

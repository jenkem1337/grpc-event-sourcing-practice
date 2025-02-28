package org.EventSource.core;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface DomainEvent {
    String toJson() throws JsonProcessingException;
}

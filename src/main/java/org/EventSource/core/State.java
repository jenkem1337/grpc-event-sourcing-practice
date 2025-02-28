package org.EventSource.core;

import java.time.Instant;
import java.util.UUID;

public interface State {
    UUID id();
    Instant createdAt();
    Instant updatedAt();
}

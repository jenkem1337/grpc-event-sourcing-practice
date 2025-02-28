package org.EventSource.core;

import java.util.UUID;

public record Snapshot(
        UUID aggregateId,
        String aggregateState,
        Long version
) {
}

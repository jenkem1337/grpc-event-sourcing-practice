package org.EventSource.core;

public class OptimisticConcurrencyException extends Exception {
    public OptimisticConcurrencyException(String message) {
        super(message);
    }
}

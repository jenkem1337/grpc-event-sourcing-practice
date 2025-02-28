package org.EventSource.example.Domain.ShoppingCart;

public enum CartState {
    CREATED("Created"),
    COMPLETED("Completed");
    private final String state;

    CartState(String state) {
        this.state = state;
    }

    public String state() {return this.state;}
}

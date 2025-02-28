package org.EventSource.example.Domain.ShoppingCart.ValueObject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Quantity {

    private final Integer quantity;

    @JsonCreator
    public Quantity(@JsonProperty("quantity") Integer quantity) throws Exception {
        if(quantity < 0) {
            throw  new Exception("Quantity must greater than zero");
        }

        this.quantity = quantity;
    }

    public static Quantity create(Integer quantity) throws Exception {
        return new Quantity(quantity);
    }

    public Quantity incrementQuantity() throws Exception {
        return new Quantity(quantity + 1);
    }
    public Quantity decrementQuantity() throws  Exception {
        return new Quantity(quantity - 1);
    }
    @JsonIgnore
    public boolean isQuantityEqualZero(){
        return quantity == 0;
    }
    public Integer getQuantity() {
        return quantity;
    }

}

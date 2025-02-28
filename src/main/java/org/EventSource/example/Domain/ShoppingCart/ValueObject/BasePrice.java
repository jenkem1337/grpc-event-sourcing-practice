package org.EventSource.example.Domain.ShoppingCart.ValueObject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class BasePrice {
    private final BigDecimal price;
    @JsonCreator
    public BasePrice(@JsonProperty("price") BigDecimal price) throws Exception {
        if(price.doubleValue() < 0) {
            throw new Exception("Price must greater than zero");
        }
        this.price = price;
    }

    public static BasePrice create(BigDecimal price) throws Exception {
        return new BasePrice(price);
    }
    public BigDecimal getPrice() {
        return price;
    }
}

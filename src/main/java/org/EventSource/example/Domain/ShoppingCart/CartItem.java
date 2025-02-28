package org.EventSource.example.Domain.ShoppingCart;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ser.Serializers;
import org.EventSource.example.Domain.ShoppingCart.ValueObject.BasePrice;
import org.EventSource.example.Domain.ShoppingCart.ValueObject.Quantity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class CartItem {
    private UUID id;
    private UUID productId;
    private String productName;
    private BasePrice basePrice;
    private Quantity quantity;
    private Instant createdAt;
    private Instant updatedAt;

    @JsonCreator
    public CartItem(
            @JsonProperty("id") UUID id,
            @JsonProperty("productId") UUID productId,
            @JsonProperty("productName") String productName,
            @JsonProperty("basePrice") BasePrice basePrice,
            @JsonProperty("quantity") Quantity quantity,
            @JsonProperty("createdAt") Instant createdAt,
            @JsonProperty("updatedAt") Instant updatedAt) throws Exception {

        if(id == null) {
            throw new Exception("Id must not be null");
        }
        if(productId == null) {
            throw new Exception("Product Id must not be null");
        }
        if(productName.isBlank()) {
            throw new Exception("Product name must not be null");
        }
        this.id = id;
        this.productId = productId;
        this.basePrice = basePrice;
        this.quantity = quantity;
        this.productName = productName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static CartItem create(UUID id,
                                  UUID productId,
                                  String productName,
                                  BasePrice basePrice,
                                  Quantity quantity) throws Exception {
        return new CartItem(id, productId, productName, basePrice, quantity, Instant.now(), Instant.now());
    }

    public UUID getId() {
        return id;
    }

    public UUID getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public BasePrice getBasePrice() {
        return basePrice;
    }

    public Quantity getQuantity() {
        return quantity;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void incrementQuantity() throws Exception {
        quantity = quantity.incrementQuantity();
    }

    public void decrementQuantity() throws Exception {
        quantity = quantity.decrementQuantity();
        }
}

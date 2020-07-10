package com.example.kafka.streams.order;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Order {
    private String id;
    private long customerId;
    private OrderState state;
    private Product product;
    private int quantity;
    private double price;
    private Long timestamp;
}

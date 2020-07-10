package com.example.kafka.streams.orderdetails;

import com.example.kafka.streams.order.Order;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class OrdersByCustomer {
    private List<Order> orders = new LinkedList<>();
}

package com.example.kafka.streams;

import com.example.kafka.streams.orderdetails.OrderValidation;
import com.example.kafka.streams.order.Order;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface OrdersBinding {
    // Stores for materialized views
    String ORDERS_VALIDATION_STORE = "ORDERS_VALIDATION_STORE";
    String ORDERS_VALIDATION_BY_STATUS_STORE = "ORDERS_VALIDATION_BY_STATUS_STORE";
    String ORDERS_BY_ID_STORE = "ORDERS_BY_ID_STORE";
    String ORDERS_BY_CUSTOMER_ID_STORE = "ORDERS_BY_CUSTOMER_ID_STORE";

    // For orderService
    String ORDERS_OUT = "orders-out";

    @Output(ORDERS_OUT)
    MessageChannel ordersOut();


    // For OrderDetailsService
    String ORDERS_IN = "orders-in";

    String ORDERS_VALIDATION_OUT = "orders-validation-out";
    String ORDERS_VALIDATION_IN = "orders-validation-in";

    @Input(ORDERS_IN)
    KStream<String, Order> ordersIn();

    @Output(ORDERS_VALIDATION_OUT)
    KStream<String, OrderValidation> ordersValidationOut();

    @Input(ORDERS_VALIDATION_IN)
    KStream<String, OrderValidation> ordersValidationIn();
}

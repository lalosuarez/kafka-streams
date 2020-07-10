package com.example.kafka.streams.orderdetails;

import com.example.kafka.streams.OrdersBinding;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.stereotype.Component;

@Component
public class OrderValidationsSink {

    private static final Logger logger = LoggerFactory.getLogger(OrderValidationsSink.class);

    @StreamListener
    public void process(
            @Input(OrdersBinding.ORDERS_VALIDATION_IN) final KStream<String, OrderValidation> orderValidations) {

        // Groups by validation status and stores the result in a materialized view
        orderValidations
                .peek((key, orderValidation) -> logger.info("Order {} validation result {}", key,
                        orderValidation.getValidationResult()))
                .groupBy((key, orderValidation) -> orderValidation.getValidationResult().name())
                .count(Materialized.as(OrdersBinding.ORDERS_VALIDATION_BY_STATUS_STORE));
    }
}
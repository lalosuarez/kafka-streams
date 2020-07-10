package com.example.kafka.streams.orderdetails;

import com.example.kafka.streams.OrdersBinding;
import com.example.kafka.streams.order.Order;
import com.example.kafka.streams.order.OrderState;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

@Component
public class OrderDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(OrderDetailsService.class);

    @StreamListener
    @SendTo({OrdersBinding.ORDERS_VALIDATION_OUT})
    public KStream<String, OrderValidation> validateOrder(
            @Input(OrdersBinding.ORDERS_IN) KStream<String, Order> orders) {

        // Groups by id result and stores the result in a materialized view
        orders
                .groupByKey(Grouped.with(Serdes.String(), new JsonSerde<>(Order.class)))
                .aggregate(
                        Order::new,
                        (s, order, newOrder) -> order,
                        Materialized.<String, Order, KeyValueStore<Bytes, byte[]>>as(OrdersBinding.ORDERS_BY_ID_STORE)
                                .withKeySerde(Serdes.String())
                                .withValueSerde(new JsonSerde<>(Order.class))
                );

        // Groups by id result and stores the result in a materialized view
        orders
                .groupBy((s, order) -> order.getCustomerId(), Grouped.with(Serdes.Long(), new JsonSerde<>(Order.class)))
                .aggregate(
                        Order::new,
                        (s, order, newOrder) -> order,
                        Materialized.<Long, Order, KeyValueStore<Bytes, byte[]>>as(OrdersBinding.ORDERS_BY_CUSTOMER_ID_STORE)
                                .withKeySerde(Serdes.Long())
                                .withValueSerde(new JsonSerde<>(Order.class))
                );

        return orders
                .filter((key, order) -> OrderState.CREATED.equals(order.getState()))
                .map((key, order) -> getOrderValidationResult(order, isValid(order) ? OrderValidationResult.PASS : OrderValidationResult.FAIL));
    }

    // TODO: Move this class
    @Component
    public static class OrderValidationsSink {

        private static final Logger logger = LoggerFactory.getLogger(OrderValidationsSink.class);

        @StreamListener
        public void process(
                @Input(OrdersBinding.ORDERS_VALIDATION_IN) KStream<String, OrderValidation> orderValidations) {

            orderValidations
                    //.toStream()
                    .foreach((key, orderValidation) -> logger.info("Order {} {}", key,
                            orderValidation.getValidationResult()));

            // Groups by validation status and stores the result in a materialized view
            orderValidations
                    .groupBy((s, orderValidation) -> orderValidation.getValidationResult().name())
                    .count(Materialized.as(OrdersBinding.ORDERS_VALIDATION_BY_STATUS_STORE));

            /*
            orderValidations
                    .groupByKey()
                    .aggregate(
                            String::new,
                            (s, domainEvent, board) -> board.concat(domainEvent.getValidationResult().toString()),
                            Materialized.<String, String, KeyValueStore<Bytes, byte[]>>as("test-events-snapshots")
                                    .withKeySerde(Serdes.String()).
                                    withValueSerde(Serdes.String())
                    );
            */
        }
    }

    /**
     *
     * @param order
     * @param passOrFail
     * @return
     */
    private KeyValue<String, OrderValidation> getOrderValidationResult(final Order order,
                                                                       final OrderValidationResult passOrFail) {
        logger.info("Validating order {}", order.getId());
        final OrderValidation value = new OrderValidation(order.getId(), OrderValidationType.ORDER_DETAILS_CHECK, passOrFail);
        logger.debug("validation result {}", value);
        return new KeyValue<>(order.getId(), value);
    }

    /**
     *
     * @param order
     * @return
     */
    private boolean isValid(final Order order) {
        if (order.getCustomerId() <= 0) {
            return false;
        }
        if (order.getQuantity() <= 0) {
            return false;
        }
        if (order.getPrice() <= 0) {
            return false;
        }
        return order.getProduct() != null;
    }
}

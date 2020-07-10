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
            @Input(OrdersBinding.ORDERS_IN) final KStream<String, Order> orders) {

        // Groups by id result and stores the result in a materialized view
        orders
                .groupByKey(Grouped.with(Serdes.String(), new JsonSerde<>(Order.class)))
                .aggregate(
                        Order::new,
                        (key, order, newOrder) -> order,
                        Materialized.<String, Order, KeyValueStore<Bytes, byte[]>>as(OrdersBinding.ORDERS_BY_ID_STORE)
                                .withKeySerde(Serdes.String())
                                .withValueSerde(new JsonSerde<>(Order.class))
                );

        // Groups by id result and stores the result in a materialized view
        orders
                .groupBy((s, order) -> order.getCustomerId(), Grouped.with(Serdes.Long(), new JsonSerde<>(Order.class)))
                .aggregate(
                        OrdersByCustomer::new,
                        (customerId, order, ordersByCustomer) -> {
                            ordersByCustomer.getOrders().add(order);
                            logger.debug("orders by customer id {} = {}", customerId, ordersByCustomer.getOrders().size());
                            return ordersByCustomer;
                        },
                        Materialized.<Long, OrdersByCustomer, KeyValueStore<Bytes, byte[]>>as(OrdersBinding.ORDERS_BY_CUSTOMER_ID_STORE)
                                .withKeySerde(Serdes.Long())
                                .withValueSerde(new JsonSerde<>(OrdersByCustomer.class))
                );

        return orders
                .filter((key, order) -> OrderState.CREATED.equals(order.getState()))
                .map((key, order) -> getOrderValidationResult(order, isValid(order) ? OrderValidationResult.PASS : OrderValidationResult.FAIL));
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
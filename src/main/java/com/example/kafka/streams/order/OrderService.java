package com.example.kafka.streams.order;

import com.example.kafka.streams.OrdersBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final MessageChannel messageChannel;

    public OrderService(OrdersBinding ordersBinding) {
        this.messageChannel = ordersBinding.ordersOut();
    }

    /**
     *
     * @param order
     * @return
     */
    public Order createOrder(final Order order) {
        order.setId(UUID.randomUUID().toString());
        order.setState(OrderState.CREATED);
        order.setTimestamp(getTimestamp());
        sendMessage(order);
        return order;
    }

    /**
     *
     * @return
     */
    private long getTimestamp() {
        return new Date().getTime();
    }

    /**
     * Sends a message to the broker
     * @param order
     */
    private void sendMessage(final Order order) {
        try {
            byte[] msgKey = order.getId().getBytes();
            logger.debug("Message key bytes = {}", msgKey);
            Message<Order> message = MessageBuilder
                    .withPayload(order)
                    .setHeader(KafkaHeaders.MESSAGE_KEY, msgKey)
                    .build();
            messageChannel.send(message);
            logger.info("Order sent {}", order);
        } catch (Exception e) {
            logger.error("Error sending order", e);
            e.printStackTrace();
        }
    }
}

package com.example.kafka.streams.order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Command API
 */
@RestController
@RequestMapping("")
public class OrderCmdController {

    private static final Logger logger = LoggerFactory.getLogger(OrderCmdController.class);

    private final OrderService orderService;

    public OrderCmdController(OrderService orderService) {
        this.orderService = orderService;
    }

    @RequestMapping(path = "/orders", method = RequestMethod.POST)
    public ResponseEntity create(@RequestBody final Order order) throws URISyntaxException {
        logger.info("Creating order {}", order);
        orderService.createOrder(order);
        return ResponseEntity.created(new URI("http://localhost:8084/v1/orders/" + order.getId())).build();
    }
}
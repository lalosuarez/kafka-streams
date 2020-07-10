package com.example.kafka.streams.order;

import com.example.kafka.streams.OrdersBinding;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Query API
 */
@RestController
@RequestMapping("/qry")
public class OrderQryController {

    private static final Logger logger = LoggerFactory.getLogger(OrderQryController.class);

    private final InteractiveQueryService interactiveQueryService;

    public OrderQryController(InteractiveQueryService interactiveQueryService) {
        this.interactiveQueryService = interactiveQueryService;
    }

    @RequestMapping(path = "/orders/validations/status", method = RequestMethod.GET)
    public ResponseEntity orderValidationByStatus() {
        logger.info("Getting orders validation count");
        ReadOnlyKeyValueStore<String, Long> store = interactiveQueryService
                .getQueryableStore(OrdersBinding.ORDERS_VALIDATION_BY_STATUS_STORE,
                        QueryableStoreTypes.keyValueStore());
        // TODO: Create dto
        Map<String, Long> map = new HashMap<>();
        KeyValueIterator<String, Long> iterator = store.all();
        while (iterator.hasNext()) {
            KeyValue<String, Long> item = iterator.next();
            map.put(item.key.toLowerCase(), item.value);
        }
        return ResponseEntity.ok().body(map);
    }

    @RequestMapping(path = "/orders", method = RequestMethod.GET)
    public ResponseEntity orders() {
        logger.info("Getting orders");
        ReadOnlyKeyValueStore<String, Order> store = interactiveQueryService
                .getQueryableStore(OrdersBinding.ORDERS_BY_ID_STORE,
                        QueryableStoreTypes.keyValueStore());
        List<Order> list = new LinkedList<>();
        KeyValueIterator<String, Order> iterator = store.all();
        while (iterator.hasNext()) {
            KeyValue<String, Order> item = iterator.next();
            list.add(item.value);
        }
        // TODO: Create dto
        Map<String, Object> map = new HashMap<>();
        map.put("total", list.size());
        map.put("orders", list);
        return ResponseEntity.ok().body(map);
    }

    @RequestMapping(path = "/orders/customers/{id}", method = RequestMethod.GET)
    public ResponseEntity ordersByCustomerId(@PathVariable("id") Long id) {
        logger.info("Getting orders by customer {}", id);
        ReadOnlyKeyValueStore<Long, Order> store = interactiveQueryService
                .getQueryableStore(OrdersBinding.ORDERS_BY_CUSTOMER_ID_STORE,
                        QueryableStoreTypes.keyValueStore());
        final Order order = store.get(id);
        return ResponseEntity.ok().body(order);
    }
}

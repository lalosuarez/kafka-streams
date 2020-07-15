package com.example.kafka.streams.order;

import com.example.kafka.streams.OrdersBinding;
import com.example.kafka.streams.orderdetails.OrderValidation;
import com.example.kafka.streams.orderdetails.OrdersByCustomer;
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

    @RequestMapping(path = "/orders/validations", method = RequestMethod.GET)
    public ResponseEntity ordersValidation() {
        logger.info("Getting orders validation");

        final ReadOnlyKeyValueStore<String, OrderValidation> store = interactiveQueryService
                .getQueryableStore(OrdersBinding.ORDERS_VALIDATION_BY_ID_STORE,
                        QueryableStoreTypes.keyValueStore());

        final List<OrderValidation> list = new LinkedList<>();
        final KeyValueIterator<String, OrderValidation> iterator = store.all();
        while (iterator.hasNext()) {
            KeyValue<String, OrderValidation> item = iterator.next();
            list.add(item.value);
        }
        return ResponseEntity.ok()
                .body(new ListResponse<>((long) list.size(), list));
    }

    @RequestMapping(path = "/orders/validations/status", method = RequestMethod.GET)
    public ResponseEntity ordersValidationByStatus() {
        logger.info("Getting orders validation count");

        final ReadOnlyKeyValueStore<String, Long> store = interactiveQueryService
                .getQueryableStore(OrdersBinding.ORDERS_VALIDATION_BY_STATUS_STORE,
                        QueryableStoreTypes.keyValueStore());
        // TODO: Create dto
        final Map<String, Long> map = new HashMap<>();
        final KeyValueIterator<String, Long> iterator = store.all();
        while (iterator.hasNext()) {
            KeyValue<String, Long> item = iterator.next();
            map.put(item.key.toLowerCase(), item.value);
        }

        return ResponseEntity.ok().body(map);
    }

    @RequestMapping(path = "/orders", method = RequestMethod.GET)
    public ResponseEntity orders() {
        logger.info("Getting orders");

        final ReadOnlyKeyValueStore<String, Order> store = interactiveQueryService
                .getQueryableStore(OrdersBinding.ORDERS_BY_ID_STORE,
                        QueryableStoreTypes.keyValueStore());

        final List<Order> list = new LinkedList<>();
        final KeyValueIterator<String, Order> iterator = store.all();
        while (iterator.hasNext()) {
            KeyValue<String, Order> item = iterator.next();
            list.add(item.value);
        }
        return ResponseEntity.ok()
                .body(new ListResponse((long)list.size(), list));
    }

    @RequestMapping(path = "/orders/customers/{id}", method = RequestMethod.GET)
    public ResponseEntity ordersByCustomerId(@PathVariable("id") final Long id) {
        logger.info("Getting orders by customer {}", id);

        final ReadOnlyKeyValueStore<Long, OrdersByCustomer> store = interactiveQueryService
                .getQueryableStore(OrdersBinding.ORDERS_BY_CUSTOMER_ID_STORE,
                        QueryableStoreTypes.keyValueStore());

        final OrdersByCustomer ordersByCustomer = store.get(id);
        if (ordersByCustomer == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .body(new ListResponse((long)ordersByCustomer.getOrders().size(),
                        ordersByCustomer.getOrders()));
    }
}

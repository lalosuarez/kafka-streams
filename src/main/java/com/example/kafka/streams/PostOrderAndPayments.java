package com.example.kafka.streams;

import com.example.kafka.streams.order.Order;
import com.example.kafka.streams.order.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class PostOrderAndPayments implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(PostOrderAndPayments.class);

    private final RestTemplate restTemplate;

    public PostOrderAndPayments(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        final String URL = "http://localhost:8084/v1/orders";
        final int NUM_CUSTOMERS = 5;
        final int NUM_QUANTITY = 3;
        final List<Product> productTypeList = Arrays.asList(Product.JUMPERS, Product.UNDERPANTS, Product.STOCKINGS);
        final Random randomGenerator = new Random();

        final Runnable runnable = () -> {
            int randomCustomerId = randomGenerator.nextInt(NUM_CUSTOMERS);
            int randomQuantity = randomGenerator.nextInt(NUM_QUANTITY);
            final Product randomProduct = productTypeList.get(randomGenerator.nextInt(productTypeList.size()));
            final Order order = new Order();
            order.setCustomerId(randomCustomerId);
            order.setProduct(randomProduct);
            order.setQuantity(randomQuantity);
            order.setPrice(5d);
            logger.debug("Posting order {} to {}", order, URL);
            try {
                ResponseEntity<String> response = restTemplate
                        .postForEntity(URL, order, String.class);
                logger.debug("Posting order response {}", response);
            } catch (RestClientException e) {
                logger.error("Posting order error", e);
            }
        };
        Executors.newScheduledThreadPool(1)
                .scheduleAtFixedRate(runnable, 3, 30, TimeUnit.SECONDS);
    }
}

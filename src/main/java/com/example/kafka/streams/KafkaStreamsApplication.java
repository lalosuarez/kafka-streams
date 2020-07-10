package com.example.kafka.streams;

import org.apache.kafka.common.serialization.Serdes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableBinding(OrdersBinding.class)
public class KafkaStreamsApplication {

	private static final Logger logger = LoggerFactory.getLogger(KafkaStreamsApplication.class);

	public static void main(String[] args) {
		logger.debug("serde for string {}", Serdes.String().getClass().getName());
		logger.debug("serde for json {}",  new JsonSerde().getClass().getName());
		logger.debug(JsonDeserializer.KEY_DEFAULT_TYPE);
		logger.debug(JsonDeserializer.VALUE_DEFAULT_TYPE);
		logger.debug(JsonDeserializer.TRUSTED_PACKAGES);
		SpringApplication.run(KafkaStreamsApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
	
}
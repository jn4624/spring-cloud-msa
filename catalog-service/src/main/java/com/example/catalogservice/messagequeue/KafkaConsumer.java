package com.example.catalogservice.messagequeue;

import com.example.catalogservice.entity.CatalogEntity;
import com.example.catalogservice.jpa.CatalogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class KafkaConsumer {
    private final CatalogRepository catalogRepository;

    public KafkaConsumer(CatalogRepository catalogRepository) {
        this.catalogRepository = catalogRepository;
    }

    @KafkaListener(topics = "example-catalog-topic", groupId = "consumerGroupId")
    public void updateQty(String kafkaMessage) {
        log.info("* Kafka Message: {}", kafkaMessage);

        Map<Object, Object> map = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            map = objectMapper.readValue(kafkaMessage, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        CatalogEntity catalogEntity = catalogRepository.findByProductId((String) map.get("productId"));

        if (catalogEntity != null) {
            catalogEntity.setStock(catalogEntity.getStock() - (Integer) map.get("qty"));
            catalogRepository.save(catalogEntity);
        }
    }
}

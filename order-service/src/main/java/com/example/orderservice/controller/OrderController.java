package com.example.orderservice.controller;

import com.example.orderservice.dto.OrderDto;
import com.example.orderservice.entity.OrderEntity;
import com.example.orderservice.messagequeue.KafkaProducer;
import com.example.orderservice.messagequeue.OrderProducer;
import com.example.orderservice.service.OrderService;
import com.example.orderservice.vo.RequestOrder;
import com.example.orderservice.vo.ResponseOrder;
import org.modelmapper.ModelMapper;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/order-service")
public class OrderController {
    private final Environment environment;
    private final OrderService orderService;
    private final ModelMapper modelMapper;
    private final KafkaProducer kafkaProducer;
    private final OrderProducer orderProducer;

    public OrderController(Environment environment, OrderService orderService,
                           ModelMapper modelMapper, KafkaProducer kafkaProducer, OrderProducer orderProducer) {
        this.environment = environment;
        this.orderService = orderService;
        this.modelMapper = modelMapper;
        this.kafkaProducer = kafkaProducer;
        this.orderProducer = orderProducer;
    }

    @GetMapping("/health_check")
    public String status() {
        return String.format("It's working in Order Service on PORT %s", environment.getProperty("local.server.port"));
    }

    @PostMapping("/{userId}/orders")
    public ResponseEntity<ResponseOrder> createOrder(@PathVariable(name = "userId") String userId,
                                                     @RequestBody RequestOrder requestOrder) {
        OrderDto orderDto = modelMapper.map(requestOrder, OrderDto.class);
        orderDto.setUserId(userId);
        // jpa
//        OrderDto createdOrder = orderService.createOrder(orderDto);
//        ResponseOrder result = modelMapper.map(createdOrder, ResponseOrder.class);

        // kafka
        orderDto.setOrderId(UUID.randomUUID().toString());
        orderDto.setTotalPrice(requestOrder.getUnitPrice() * requestOrder.getQty());

        // send this order to the kafka
        kafkaProducer.send("example-catalog-topic", orderDto);
        // send this order to the kafka sink connect
        orderProducer.send("orders", orderDto);

        ResponseOrder result = modelMapper.map(orderDto, ResponseOrder.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/{userId}/orders")
    public ResponseEntity<List<ResponseOrder>> getOrder(@PathVariable(name = "userId") String userId) {
        Iterable<OrderEntity> orderList = orderService.getOrdersByUserId(userId);

        List<ResponseOrder> results = new ArrayList<>();
        orderList.forEach(o -> {
            results.add(modelMapper.map(o, ResponseOrder.class));
        });

        return ResponseEntity.status(HttpStatus.OK).body(results);
    }
}

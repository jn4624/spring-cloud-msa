package com.example.userservice.service;

import com.example.userservice.client.OrderServiceClient;
import com.example.userservice.dto.UserDto;
import com.example.userservice.jpa.UserEntity;
import com.example.userservice.jpa.UserRepository;
import com.example.userservice.vo.ResponseOrder;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final ModelMapper modelMapper;
//    private final RestTemplate restTemplate;
//    private final Environment environment;
    private final OrderServiceClient orderServiceClient;
    private final CircuitBreakerFactory circuitBreakerFactory;

    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder,
                           ModelMapper modelMapper, OrderServiceClient orderServiceClient,
                           CircuitBreakerFactory circuitBreakerFactory) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.modelMapper = modelMapper;
        this.orderServiceClient = orderServiceClient;
        this.circuitBreakerFactory = circuitBreakerFactory;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByEmail(username);

        if (userEntity == null) {
            throw new UsernameNotFoundException("User not found - username: " + username);
        }

        return new User(userEntity.getEmail(), userEntity.getEncryptedPwd(),
                true, true, true, true, new ArrayList<>());
    }

    @Override
    public UserDto getUserDetailsByEmail(String email) {
        UserEntity userEntity = userRepository.findByEmail(email);

        if (userEntity == null) {
            throw new UsernameNotFoundException("User not found - email: " + email);
        }
        return modelMapper.map(userEntity, UserDto.class);
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        userDto.setUserId(UUID.randomUUID().toString());

        UserEntity userEntity = modelMapper.map(userDto, UserEntity.class);
        userEntity.setEncryptedPwd(bCryptPasswordEncoder.encode(userDto.getPwd()));
        userRepository.save(userEntity);

        return modelMapper.map(userEntity, UserDto.class);
    }

    @Override
    public Iterable<UserEntity> getUserByAll() {
        return userRepository.findAll();
    }

    @Override
    public UserDto getUserByUserId(String userId) {
        UserEntity userEntity = userRepository.findByUserId(userId);

        if (userEntity == null) {
            throw new UsernameNotFoundException("User not found");
        }

        UserDto userDto = modelMapper.map(userEntity, UserDto.class);

//        List<ResponseOrder> orders = new ArrayList<>();

        // Using as rest template
//        String orderServiceURL = String.format(environment.getProperty("order_service.url"), userId);
//        ResponseEntity<List<ResponseOrder>> responseOrderList = restTemplate.exchange(orderServiceURL,
//                HttpMethod.GET,
//                null,
//                new ParameterizedTypeReference<>() {
//                });
//
//        List<ResponseOrder> orders = responseOrderList.getBody();

        // Using a feign client
        // Feign exception handling
//        List<ResponseOrder> orders = null;
//        try {
//            orders = orderServiceClient.getOrders(userId);
//        } catch (FeignException e) {
//            log.error("* {}", e.getMessage());
//        }

        // ErrorDecoder
//        List<ResponseOrder> orders = orderServiceClient.getOrders(userId);

        // CircuitBreaker
        CircuitBreaker circuitBreaker = circuitBreakerFactory.create("circuitBreaker");
        List<ResponseOrder> orders = circuitBreaker.run(() -> orderServiceClient.getOrders(userId), throwable -> new ArrayList<>());

        userDto.setOrders(orders);
        return userDto;
    }
}

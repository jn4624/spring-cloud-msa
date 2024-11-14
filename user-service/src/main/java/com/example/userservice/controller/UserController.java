package com.example.userservice.controller;

import com.example.userservice.dto.UserDto;
import com.example.userservice.jpa.UserEntity;
import com.example.userservice.service.UserService;
import com.example.userservice.vo.Greeting;
import com.example.userservice.vo.RequestUser;
import com.example.userservice.vo.ResponseUser;
import org.modelmapper.ModelMapper;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/user-service")
public class UserController {
    private final Environment environment;
    private final Greeting greeting;
    private final UserService userService;
    private final ModelMapper modelMapper;

    public UserController(Environment environment, Greeting greeting, ModelMapper modelMapper, UserService userService) {
        this.environment = environment;
        this.greeting = greeting;
        this.modelMapper = modelMapper;
        this.userService = userService;
    }

    @GetMapping("/health_check")
    public String status() {
        return String.format("It's working in User Service on PORT %s", environment.getProperty("local.server.port"));
    }

    @GetMapping("/welcome")
    public String welcome() {
//        return environment.getProperty("greeting.message");
        return greeting.getMessage();
    }

    @PostMapping("/users")
    public ResponseEntity<ResponseUser> createUser(@RequestBody RequestUser requestUser) {
        UserDto userDto = modelMapper.map(requestUser, UserDto.class);

        userService.createUser(userDto);

        ResponseUser responseUser = modelMapper.map(userDto, ResponseUser.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseUser);
    }

    @GetMapping("/users")
    public ResponseEntity<List<ResponseUser>> getUsers() {
        Iterable<UserEntity> users = userService.getUserByAll();

        List<ResponseUser> results = new ArrayList<>();
        users.forEach(v -> {
            results.add(modelMapper.map(v, ResponseUser.class));
        });

        return ResponseEntity.status(HttpStatus.OK).body(results);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<ResponseUser> getUser(@PathVariable(name = "userId") String userId) {
        UserDto userDto = userService.getUserByUserId(userId);

        ResponseUser result = modelMapper.map(userDto, ResponseUser.class);

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}

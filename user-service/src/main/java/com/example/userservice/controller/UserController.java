package com.example.userservice.controller;

import com.example.userservice.vo.Greeting;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class UserController {
//    private final Environment environment;
//
//    public UserController(Environment environment) {
//        this.environment = environment;
//    }
    private final Greeting greeting;

    public UserController(Greeting greeting) {
        this.greeting = greeting;
    }

    @GetMapping("/health-check")
    public String status() {
        return "It's working in User Service";
    }

    @GetMapping("/welcome")
    public String welcome() {
//        return environment.getProperty("greeting.message");
        return greeting.getMessage();
    }
}

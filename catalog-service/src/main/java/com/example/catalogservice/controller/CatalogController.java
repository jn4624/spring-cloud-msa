package com.example.catalogservice.controller;

import com.example.catalogservice.entity.CatalogEntity;
import com.example.catalogservice.service.CatalogService;
import com.example.catalogservice.vo.ResponseCatalog;
import org.modelmapper.ModelMapper;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/catalog-service")
public class CatalogController {
    private final Environment environment;
    private final CatalogService catalogService;
    private final ModelMapper modelMapper;

    public CatalogController(Environment environment, CatalogService catalogService, ModelMapper modelMapper) {
        this.environment = environment;
        this.catalogService = catalogService;
        this.modelMapper = modelMapper;
    }

    @GetMapping("/health_check")
    public String status() {
        return String.format("It's working in Catalog Service on PORT %s", environment.getProperty("local.server.port"));
    }

    @GetMapping("/catalogs")
    public ResponseEntity<List<ResponseCatalog>> getUsers() {
        Iterable<CatalogEntity> users = catalogService.getAllCatalogs();

        List<ResponseCatalog> results = new ArrayList<>();
        users.forEach(v -> {
            results.add(modelMapper.map(v, ResponseCatalog.class));
        });

        return ResponseEntity.status(HttpStatus.OK).body(results);
    }
}

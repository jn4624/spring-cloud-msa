package com.example.catalogservice.service;

import com.example.catalogservice.entity.CatalogEntity;
import com.example.catalogservice.jpa.CatalogRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Data
@Slf4j
@Service
public class CatalogServiceImpl implements CatalogService {
    private final CatalogRepository catalogRepository;
    private final ModelMapper modelMapper;

    public CatalogServiceImpl(CatalogRepository catalogRepository, ModelMapper modelMapper) {
        this.catalogRepository = catalogRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public Iterable<CatalogEntity> getAllCatalogs() {
        return catalogRepository.findAll();
    }
}

package com.graphql.repo.query;

import java.util.List;

import com.graphql.entity.queryentity.trace.TraceDTO;

import io.quarkus.mongodb.panache.PanacheMongoRepository;

import jakarta.enterprise.context.ApplicationScoped;


@ApplicationScoped
public class TraceQueryRepo implements PanacheMongoRepository<TraceDTO> {

public List<TraceDTO> findByServiceName(String serviceName) {
        return list("serviceName", serviceName);
    }

    }


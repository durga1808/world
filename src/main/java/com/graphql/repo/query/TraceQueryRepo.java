package com.graphql.repo.query;

import com.graphql.entity.queryentity.trace.TraceDTO;

import io.quarkus.mongodb.panache.PanacheMongoRepository;

import jakarta.enterprise.context.ApplicationScoped;


@ApplicationScoped
public class TraceQueryRepo implements PanacheMongoRepository<TraceDTO> {
    }


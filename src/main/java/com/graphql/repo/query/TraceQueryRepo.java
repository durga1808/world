package com.graphql.repo.query;

import com.graphql.entity.oteltrace.OtelTrace;

import io.quarkus.mongodb.panache.PanacheMongoRepository;

import jakarta.enterprise.context.ApplicationScoped;


@ApplicationScoped
public class TraceQueryRepo implements PanacheMongoRepository<OtelTrace> {
    }


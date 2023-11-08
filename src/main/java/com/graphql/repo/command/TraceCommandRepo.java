package com.graphql.repo.command;

import com.graphql.entity.oteltrace.OtelTrace;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;



@ApplicationScoped
public class TraceCommandRepo implements PanacheMongoRepository<OtelTrace>{
    
}
package com.graphql.repo;

import com.graphql.entity.otellog.OtelLog;


import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;



@ApplicationScoped
public class LogQueryRepo implements PanacheMongoRepository<OtelLog> {
    
}

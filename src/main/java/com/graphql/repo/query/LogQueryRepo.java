package com.graphql.repo.query;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;


import com.graphql.entity.queryentity.log.LogDTO;

import io.quarkus.mongodb.panache.PanacheMongoRepository;

import jakarta.enterprise.context.ApplicationScoped;



@ApplicationScoped
public class LogQueryRepo implements PanacheMongoRepository<LogDTO> {
    
public List<LogDTO> findByServiceName(String serviceName) {
        return list("serviceName", serviceName);

 
}
}
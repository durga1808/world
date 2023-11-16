package com.graphql.repo.query;

import java.util.List;

import com.graphql.entity.otellog.OtelLog;


import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;



@ApplicationScoped
public class LogQueryRepo implements PanacheMongoRepository<OtelLog> {
    


      public List<OtelLog> findByServiceName(String serviceName) {
        return list("serviceName",serviceName);
    }

   
}

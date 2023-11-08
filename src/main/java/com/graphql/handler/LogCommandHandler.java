package com.graphql.handler;

import com.graphql.entity.otellog.OtelLog;
import com.graphql.entity.queryentity.log.LogDTO;
import com.graphql.repo.LogCommandRepo;
import com.graphql.repo.LogQueryRepo;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
@ApplicationScoped
public class LogCommandHandler {
    
   @Inject
   LogCommandRepo logCommandRepo;


    public void addProductDetails(OtelLog otelLog) {
     logCommandRepo.persist(otelLog);
    
    }
}

package com.graphql.controller.query;

import java.util.List;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;

import com.graphql.entity.otellog.OtelLog;

import com.graphql.handler.query.LogQueryHandler;
import com.graphql.repo.query.LogQueryRepo;

import jakarta.inject.Inject;
@GraphQLApi
public class LogQuerycontroller{
    
 @Inject
 LogQueryHandler logQueryHandler;
 
 @Inject 
 LogQueryRepo logQueryRepo;




   @Query("getAllLogsData")
   public List<OtelLog> getAllLogData(){
       return logQueryHandler.getAllLogs();
   }
   
   public List<OtelLog> getLogsForService(@Name("serviceName") String serviceName) {
    // Call the repository method to perform the search
    return logQueryRepo.findByServiceName( serviceName);
}

  }
  






   

package com.graphql.controller.query;

import java.util.List;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;


import com.graphql.entity.queryentity.log.LogDTO;
import com.graphql.handler.query.LogQueryHandler;
import com.graphql.repo.query.LogQueryRepo;

import jakarta.inject.Inject;
import jakarta.ws.rs.PathParam;
@GraphQLApi
public class LogQuerycontroller{
    
 @Inject
 LogQueryHandler logQueryHandler;
 
 @Inject 
 LogQueryRepo logQueryRepo;




   @Query("getAllLogsData")
   public List<LogDTO> getAllLogData(){
       return logQueryHandler.getAllLogs();
   }
   
   @Query
   public List<LogDTO> getlogByServiceName(@Name("serviceName") String serviceName) {
    // Call the repository method to perform the search
    return logQueryHandler.getlogByServiceName(serviceName);
}

  }
  






   

package com.graphql.controller.query;

import java.util.List;

import org.eclipse.microprofile.graphql.GraphQLApi;
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


  }
  






   

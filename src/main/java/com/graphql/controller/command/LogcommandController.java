package com.graphql.controller.command;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;

import com.graphql.entity.queryentity.log.LogDTO;
import com.graphql.handler.command.LogCommandHandler;
import com.graphql.repo.command.LogCommandRepo;

import jakarta.inject.Inject;
@GraphQLApi
public class LogcommandController {
    
@Inject
 LogCommandHandler logCommandHandler;

 @Inject
 LogCommandRepo logCommandRepo;

   @Mutation
   public LogDTO createProduct(LogDTO logDTO){
    LogDTO log =new LogDTO();
     logCommandHandler.addProductDetails(logDTO);
        return log;
   }
}

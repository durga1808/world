package com.graphql.handler.command;


import com.graphql.entity.queryentity.log.LogDTO;
import com.graphql.repo.command.LogCommandRepo;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
@ApplicationScoped
public class LogCommandHandler {
    
   @Inject
   LogCommandRepo logCommandRepo;


    public void addProductDetails(LogDTO logDTO) {
     logCommandRepo.persist(logDTO);
    
    }
}

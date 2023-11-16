package com.graphql.handler.query;



import java.util.List;


import com.graphql.entity.queryentity.log.LogDTO;

import com.graphql.repo.query.LogQueryRepo;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;


@ApplicationScoped
public class LogQueryHandler {
     @Inject
     LogQueryRepo logQueryRepo;


    public List<LogDTO> getAllLogs(){
        List<LogDTO> logs =logQueryRepo.listAll();
        return logs;
    }
   
    public List<LogDTO> getlogByServiceName(String serviceName) {
        return logQueryRepo.findByServiceName(serviceName);
    }









   
}

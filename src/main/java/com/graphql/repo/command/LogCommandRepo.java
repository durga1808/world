package com.graphql.repo.command;



import com.graphql.entity.queryentity.log.LogDTO;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
@ApplicationScoped
public class LogCommandRepo  implements PanacheMongoRepository<LogDTO>{
    
}

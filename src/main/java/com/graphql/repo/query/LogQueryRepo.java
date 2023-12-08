package com.graphql.repo.query;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;


import com.graphql.entity.queryentity.log.LogDTO;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;



@ApplicationScoped
public class LogQueryRepo implements PanacheMongoRepository<LogDTO> {
    
public List<LogDTO> findByServiceName(String serviceName) {
        return list("serviceName", serviceName);

 
}

public List<LogDTO> findAllOrderByCreatedTimeDesc(List<String> serviceNameList) {
        return find("serviceName in ?1",Sort.descending("createdTime"),serviceNameList).list();
    }

    public List<LogDTO> findAllOrderByCreatedTimeAsc(List<String> serviceNameList) {
        return find("serviceName in ?1",Sort.ascending("createdTime"),serviceNameList).list();
    }


 }
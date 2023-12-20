package com.graphql.repo.query;

import java.util.List;

import com.graphql.entity.queryentity.trace.TraceDTO;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;


@ApplicationScoped
public class TraceQueryRepo implements PanacheMongoRepository<TraceDTO> {

public List<TraceDTO> findByServiceName(String serviceName) {
        return list("serviceName", serviceName);
    }

    
     public List<TraceDTO> findAllOrderByCreatedTimeDesc(List<String> serviceNameList) {
      return find("serviceName in ?1",Sort.descending("createdTime"),serviceNameList).list();
    }

    public List<TraceDTO> findAllOrderByCreatedTimeAsc(List<String> serviceNameList) {
        return find("serviceName in ?1",Sort.ascending("createdTime"),serviceNameList).list();
    }

       
    }


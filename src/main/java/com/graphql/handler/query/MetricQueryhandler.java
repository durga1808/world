package com.graphql.handler.query;


import java.time.LocalDate;
import java.util.List;

import com.graphql.entity.queryentity.metric.MetricDTO;
import com.graphql.repo.query.MetricQueryRepo;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class MetricQueryhandler {


    @Inject
    MetricQueryRepo metricQueryRepo;


   
    public List<MetricDTO> getAllMetricData() {
        return metricQueryRepo.listAll();
    }



    public List<MetricDTO> getByServiceName(String serviceName, LocalDate fromDate, LocalDate toDate, Integer minutesAgo) {
        return metricQueryRepo.findByServiceName(serviceName, fromDate, toDate, minutesAgo);
    }

    
}

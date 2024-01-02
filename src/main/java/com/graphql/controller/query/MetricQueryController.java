package com.graphql.controller.query;

import java.time.LocalDate;
import java.util.List;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;

import com.graphql.entity.queryentity.metric.MetricDTO;
import com.graphql.handler.query.MetricQueryhandler;

import jakarta.inject.Inject;

@GraphQLApi
public class MetricQueryController {
    @Inject
    MetricQueryhandler metricQueryhandler;
    
    
    
    
    
  @Query("getAllMetricData")
  public List<MetricDTO> getAllMetricDatas() {
        return metricQueryhandler.getAllMetricData();
    }


    @Query
      public List<MetricDTO> metricDataByServiceName(@Name("serviceName") String serviceName,
        @Name("from") LocalDate fromDate,
        @Name("to") LocalDate toDate,
        @Name("minutesAgo") Integer minutesAgo) {
       return metricQueryhandler.getByServiceName(serviceName,fromDate,toDate,minutesAgo);
    }

}

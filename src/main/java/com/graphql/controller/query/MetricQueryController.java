package com.graphql.controller.query;

import java.util.List;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

import com.graphql.entity.otelmetric.OtelMetric;
import com.graphql.handler.query.MetricQueryhandler;

import jakarta.inject.Inject;

@GraphQLApi
public class MetricQueryController {
    @Inject
        MetricQueryhandler metricQueryhandler;
    
    
    
    
    
        @Query("getAllMetricData")
        public List<OtelMetric> getAllMetricData(){
         List<OtelMetric> otelMetrics =  metricQueryhandler.getAllData();
           List<OtelMetric> otelMetricsl;
        return otelMetrics;
        }
}

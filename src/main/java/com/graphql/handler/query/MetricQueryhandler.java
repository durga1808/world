package com.graphql.handler.query;

import java.util.List;

import com.graphql.entity.otelmetric.OtelMetric;

import com.graphql.repo.query.MetricQueryRepo;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class MetricQueryhandler {
    @Inject
    MetricQueryRepo metricQueryRepo;


    public List<OtelMetric> getAllData(){
        List<OtelMetric> otelMetrics = metricQueryRepo.listAll();
        return otelMetrics;
    }
}

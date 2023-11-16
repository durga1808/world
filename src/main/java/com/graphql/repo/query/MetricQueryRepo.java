package com.graphql.repo.query;

import com.graphql.entity.otelmetric.OtelMetric;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
@ApplicationScoped
public class MetricQueryRepo  implements PanacheMongoRepository<OtelMetric>{
    
}

package com.graphql.repo.query;

import com.graphql.entity.queryentity.kepler.KeplerMetricDTO;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class KeplerMetricRepo implements PanacheMongoRepository<KeplerMetricDTO> {
    
}

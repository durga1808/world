package com.graphql.handler.query;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.graphql.entity.queryentity.trace.TraceDTO;
import com.graphql.repo.query.TraceQueryRepo;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class TraceQueryHandler {
    @Inject
    TraceQueryRepo traceQueryRepo;

    List<TraceDTO> traceDTOs = new ArrayList<>();

    public List<TraceDTO> getAllTraceData() {
        List<TraceDTO> allTraces = traceQueryRepo.listAll();
        return allTraces;
    }



    public List<TraceDTO> getByServiceName(String serviceName) {
        return traceQueryRepo.findByServiceName(serviceName);
    }
}
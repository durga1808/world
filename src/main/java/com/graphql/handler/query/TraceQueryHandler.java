package com.graphql.handler.query;

import java.util.ArrayList;
import java.util.List;

import com.graphql.entity.oteltrace.OtelTrace;
import com.graphql.repo.query.TraceQueryRepo;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class TraceQueryHandler {
    @Inject
    TraceQueryRepo traceQueryRepo;

    List<OtelTrace> otelTrace = new ArrayList<>();

    public List<OtelTrace> getAllTraceData() {
        List<OtelTrace> allTraces = traceQueryRepo.listAll();
        return allTraces;
    }
}

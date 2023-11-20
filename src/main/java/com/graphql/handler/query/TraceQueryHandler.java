package com.graphql.handler.query;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.graphql.entity.queryentity.trace.TraceDTO;
import com.graphql.repo.query.TraceQueryRepo;

import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.panache.common.Parameters;
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

     public List<TraceDTO> getTracesByStatusCodeRange(Integer minStatusCode, Integer maxStatusCode) {
        PanacheQuery<TraceDTO> query = TraceDTO.find("statusCode >= :minStatusCode and statusCode <= :maxStatusCode",
                Parameters.with("minStatusCode", minStatusCode)
                        .and("maxStatusCode", maxStatusCode));

        // Filter out any data that falls outside the specified range
        return query.list().stream()
                .filter(trace -> trace.getStatusCode() >= minStatusCode && trace.getStatusCode() <= maxStatusCode)
                .collect(Collectors.toList());
    }
}
package com.graphql.controller.query;

import java.time.LocalDate;
import java.util.List;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;

import com.graphql.entity.queryentity.trace.TraceDTO;
import com.graphql.entity.queryentity.trace.TraceQuery;
import com.graphql.handler.query.TraceQueryHandler;
import com.graphql.repo.query.TraceQueryRepo;

import jakarta.inject.Inject;
import jakarta.ws.rs.PathParam;


@GraphQLApi
public class TraceQueryController {
    @Inject
    TraceQueryRepo traceQueryRepo;

    @Inject
    TraceQueryHandler traceQueryHandler;

    // @Query("getAllTraceData")
    // public List<TraceDTO> getAllTraces() {
    //     return traceQueryHandler.getAllTraceData();
    // }

    // @Query
    //   public List<TraceDTO> getByServiceName(@Name("serviceName") String serviceName) {
    //    return traceQueryHandler.getByServiceName(serviceName);
    // }

    // @Query
    // public List<TraceDTO> getTracesByStatusCodeRange(@Name("minStatusCode") Integer minStatusCode,@Name("maxStatusCode") Integer maxStatusCode) {
    //     return traceQueryHandler.getTracesByStatusCodeRange(minStatusCode, maxStatusCode);
    // }


// @Query
    // public List<TraceDTO> getTracesByStatusCodeAndDuration(
    //         @Name("minStatusCode") Integer minStatusCode,
    //         @Name("maxStatusCode") Integer maxStatusCode,
    //         @Name("duration") QueryDuration duration,
    //         @Name("serviceNames") List<String> serviceNames,
    //         @Name("methodNames") List<String> methodNames) {
    
    //     return traceQueryHandler.getTracesByStatusCodeAndDuration(minStatusCode, maxStatusCode, duration, serviceNames, methodNames);
    // }
    
    
    @Query
    public List<TraceDTO> getTracesByStatusCodeAndDuration(@Name("query") TraceQuery query) {
        return traceQueryHandler.getTracesByStatusCodeAndDuration(query);
    }


    
}


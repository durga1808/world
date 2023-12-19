package com.graphql.controller.query;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;

import com.graphql.entity.queryentity.trace.TraceDTO;
import com.graphql.entity.queryentity.trace.TraceQuery;
import com.graphql.handler.query.TraceQueryHandler;
import com.graphql.repo.query.TraceQueryRepo;

import jakarta.inject.Inject;


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
public List<TraceDTO> getTracesByStatusCodeAndDuration(
        @Name("query") TraceQuery query,
        @Name("page") int page,
        @Name("pagesize") int pageSize,
        @Name("from") LocalDate fromDate,
        @Name("to") LocalDate toDate,
        @Name("minutesAgo") Integer minutesAgo,
        @Name("sortorder") String sortOrder) {

    List<TraceDTO> traceList = new ArrayList<>();  

    if (sortOrder != null) {
        if ("new".equalsIgnoreCase(sortOrder)) {
            traceList = traceQueryHandler.getTraceFilterOrderByCreatedTimeDesc(traceQueryHandler.getTracesByStatusCodeAndDuration(query, page, pageSize, fromDate, toDate, minutesAgo));
        } else if ("old".equalsIgnoreCase(sortOrder)) {
            traceList = traceQueryHandler.getTraceFilterAsc(traceQueryHandler.getTracesByStatusCodeAndDuration(query, page, pageSize, fromDate, toDate, minutesAgo));
        } else if ("error".equalsIgnoreCase(sortOrder)) {
            traceList = traceQueryHandler.getTraceFilterOrderByErrorFirst(traceQueryHandler.getTracesByStatusCodeAndDuration(query, page, pageSize, fromDate, toDate, minutesAgo));
        } else if ("peakLatency".equalsIgnoreCase(sortOrder)) {
            traceList = traceQueryHandler.getTraceFilterOrderByDuration(traceQueryHandler.getTracesByStatusCodeAndDuration(query, page, pageSize, fromDate, toDate, minutesAgo));
        } 
    } else {
        traceList = traceQueryHandler.getTracesByStatusCodeAndDuration(query, page, pageSize, fromDate, toDate, minutesAgo);
    }

    return traceList;
}

    


    
}


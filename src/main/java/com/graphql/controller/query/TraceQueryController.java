package com.graphql.controller.query;

import java.util.List;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

import com.graphql.entity.queryentity.trace.TraceDTO;
import com.graphql.handler.query.TraceQueryHandler;
import com.graphql.repo.query.TraceQueryRepo;

import jakarta.inject.Inject;


@GraphQLApi
public class TraceQueryController {
    @Inject
    TraceQueryRepo traceQueryRepo;

    @Inject
    TraceQueryHandler traceQueryHandler;

    @Query("getAllTrace")
    public List<TraceDTO> getAllTraces() {
        return traceQueryHandler.getAllTraceData();
    }
}


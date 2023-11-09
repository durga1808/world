package com.graphql.controller.command;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;

import com.graphql.entity.oteltrace.OtelTrace;
import com.graphql.handler.command.TraceCommandHandler;
import com.graphql.repo.command.TraceCommandRepo;

import jakarta.inject.Inject;

@GraphQLApi
public class TraceCommandController {
    @Inject
    TraceCommandRepo traceCommandRepo;

    @Inject
    TraceCommandHandler traceCommandHandler;

    @Mutation
    public OtelTrace createTrace(OtelTrace otelTrace) {
        OtelTrace trace = new OtelTrace();
        traceCommandHandler.addTraceData(otelTrace);
        return trace;
    }
}

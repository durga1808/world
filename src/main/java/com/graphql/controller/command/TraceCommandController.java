// package com.graphql.controller.command;

// import org.eclipse.microprofile.graphql.GraphQLApi;
// import org.eclipse.microprofile.graphql.Mutation;

// import com.graphql.entity.queryentity.trace.TraceDTO;
// import com.graphql.handler.command.TraceCommandHandler;
// import com.graphql.repo.command.TraceCommandRepo;

// import jakarta.inject.Inject;

// @GraphQLApi
// public class TraceCommandController {
//     @Inject
//     TraceCommandRepo traceCommandRepo;

//     @Inject
//     TraceCommandHandler traceCommandHandler;

//     @Mutation
//     public TraceDTO createTrace(TraceDTO traceDTO) {
//         TraceDTO trace = new TraceDTO();
//         traceCommandHandler.addTraceData(traceDTO);
//         return trace;
//     }
// }

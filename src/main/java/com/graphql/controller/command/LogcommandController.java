// package com.graphql.controller.command;

// import org.eclipse.microprofile.graphql.GraphQLApi;
// import org.eclipse.microprofile.graphql.Mutation;

// import com.graphql.entity.otellog.OtelLog;
// import com.graphql.handler.command.LogCommandHandler;
// import com.graphql.repo.command.LogCommandRepo;

// import jakarta.inject.Inject;
// @GraphQLApi
// public class LogcommandController {
    
// @Inject
//  LogCommandHandler logCommandHandler;

//  @Inject
//  LogCommandRepo logCommandRepo;

//    @Mutation
//    public OtelLog createProduct(OtelLog otelLog){
//       OtelLog log =new OtelLog();
//      logCommandHandler.addProductDetails(otelLog);
//         return log;
//    }
// }
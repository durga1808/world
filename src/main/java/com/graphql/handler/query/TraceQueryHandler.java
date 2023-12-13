package com.graphql.handler.query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.graphql.entity.oteltrace.scopeSpans.Spans;
import com.graphql.entity.queryentity.trace.StatusCodeRange;
import com.graphql.entity.queryentity.trace.TraceDTO;
import com.graphql.entity.queryentity.trace.TraceQuery;
import com.graphql.repo.query.TraceQueryRepo;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;

import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class TraceQueryHandler {
    @Inject
    TraceQueryRepo traceQueryRepo;

    
  @Inject
  MongoClient mongoClient;

    List<TraceDTO> traceDTOs = new ArrayList<>();

    // public List<TraceDTO> getAllTraceData() {
    //     List<TraceDTO> allTraces = traceQueryRepo.listAll();
    //     return allTraces;
    // }



    // public List<TraceDTO> getByServiceName(String serviceName) {
    //     return traceQueryRepo.findByServiceName(serviceName);
    // }

    //  public List<TraceDTO> getTracesByStatusCodeRange(Integer minStatusCode, Integer maxStatusCode) {
    //     PanacheQuery<TraceDTO> query = TraceDTO.find("statusCode >= :minStatusCode and statusCode <= :maxStatusCode",
    //             Parameters.with("minStatusCode", minStatusCode)
    //                     .and("maxStatusCode", maxStatusCode));
    //     return query.list().stream()
    //             .filter(trace -> trace.getStatusCode() >= minStatusCode && trace.getStatusCode() <= maxStatusCode)
    //             .collect(Collectors.toList());
    // }


    public List<TraceDTO> getTracesByStatusCodeAndDuration(TraceQuery query) {
      PanacheQuery<TraceDTO> panacheQuery = TraceDTO.find(
              "statusCode >= :minStatusCode and statusCode <= :maxStatusCode " +
                      "and duration >= :minDuration and duration <= :maxDuration " +
                      "and serviceName in :serviceNames and methodName in :methodNames",
              Parameters
                      .with("minStatusCode", query.getStatusCode().get(0).getMin()) // Assuming there's only one value in the list
                      .and("maxStatusCode", query.getStatusCode().get(0).getMax()) // Assuming there's only one value in the list
                      .and("minDuration", query.getDuration().getMin())
                      .and("maxDuration", query.getDuration().getMax())
                      .and("serviceNames", query.getServiceName())
                      .and("methodNames", query.getMethodName()));
    
      return panacheQuery.list().stream()
              .filter(trace ->
                      trace.getStatusCode() >= query.getStatusCode().get(0).getMin() &&
                              trace.getStatusCode() <= query.getStatusCode().get(0).getMax() &&
                              trace.getDuration() >= query.getDuration().getMin() &&
                              trace.getDuration() <= query.getDuration().getMax() &&
                              query.getServiceName().contains(trace.getServiceName()) &&
                              query.getMethodName().contains(trace.getMethodName()))
              .collect(Collectors.toList());
    }
    
    
    // public List<TraceDTO> getTracesByStatusCodeAndDuration(
    //         Integer minStatusCode, Integer maxStatusCode, QueryDuration duration, List<String> serviceNames, List<String> methodNames) {
    
    //     PanacheQuery<TraceDTO> query = TraceDTO.find("statusCode >= :minStatusCode and statusCode <= :maxStatusCode " +
    //                     "and duration >= :minDuration and duration <= :maxDuration " +
    //                     "and serviceName in :serviceNames and methodName in :methodNames",
    //             Parameters.with("minStatusCode", minStatusCode)
    //                     .and("maxStatusCode", maxStatusCode)
    //                     .and("minDuration", duration.getMin())
    //                     .and("maxDuration", duration.getMax())
    //                     .and("serviceNames", serviceNames)
    //                     .and("methodNames", methodNames));
    
    //     return query.list().stream()
    //             .filter(trace -> trace.getStatusCode() >= minStatusCode && trace.getStatusCode() <= maxStatusCode &&
    //                     trace.getDuration() >= duration.getMin() && trace.getDuration() <= duration.getMax() &&
    //                     serviceNames.contains(trace.getServiceName()) &&
    //                     methodNames.contains(trace.getMethodName()))
    //             .collect(Collectors.toList());
    // }
    
    
    
    
}
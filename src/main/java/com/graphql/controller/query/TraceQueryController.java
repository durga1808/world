package com.graphql.controller.query;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;

import com.graphql.entity.oteltrace.scopeSpans.Spans;
import com.graphql.entity.queryentity.log.LogDTO;
import com.graphql.entity.queryentity.trace.DBMetric;
import com.graphql.entity.queryentity.trace.KafkaMetrics;
import com.graphql.entity.queryentity.trace.TraceDTO;
import com.graphql.entity.queryentity.trace.TraceMetrics;
import com.graphql.entity.queryentity.trace.TracePage;
import com.graphql.entity.queryentity.trace.TraceQuery;
import com.graphql.entity.queryentity.trace.TraceSpanDTO;
import com.graphql.handler.query.TraceQueryHandler;
import com.graphql.repo.query.TraceQueryRepo;

import jakarta.inject.Inject;



@GraphQLApi
public class TraceQueryController {
    @Inject
    TraceQueryRepo traceQueryRepo;

    @Inject
    TraceQueryHandler traceQueryHandler;

    @Query("getAllTraceData")
    public List<TraceDTO> getAllTraces() {
        return traceQueryHandler.getAllTraceData();
    }

    @Query
      public List<TraceDTO> getByServiceName(@Name("serviceName") String serviceName) {
       return traceQueryHandler.getByServiceName(serviceName);
    }

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
  public List<TraceMetrics> getTraceMetricCount(
     @Name("serviceNameList") List<String> serviceNameList,
     @Name("from") LocalDate fromDate,
    @Name("to") LocalDate toDate,
    @Name("minutesAgo") Integer minutesAgo
  ) {
    return traceQueryHandler.getAllTraceMetricCount(serviceNameList,fromDate,toDate,minutesAgo);
  }



  @Query
  public List<TraceMetrics> getPeakLatency(
      @Name("serviceNameList") List<String> serviceNameList,
      @Name("minpeakLatency") Long minpeakLatency,
      @Name("maxpeakLatency") Long maxpeakLatency,
      @Name("from") LocalDate fromDate,
      @Name("to") LocalDate toDate,
      @Name("minutesAgo") Integer minutesAgo
  ) {
      return  TraceQueryHandler.getPeakLatency(serviceNameList, fromDate, toDate, minutesAgo, minpeakLatency, maxpeakLatency);
  }
  


@Query
public List<DBMetric> getDBTraceMetricCount(
            @Name("from") LocalDate from,
            @Name("to") LocalDate to,
            @Name("minutesAgo") int minutesAgo,
            @Name("serviceNameList") List<String> serviceNames) {
        return traceQueryHandler.getAllDBMetrics(serviceNames, from, to, minutesAgo);
    }

@Query
public List<DBMetric> getDBTracePeakLatencyCount(
            @Name("from") LocalDate from,
            @Name("to") LocalDate to,
            @Name("minutesAgo") int minutesAgo,
            @Name("serviceNameList") List<String> serviceNames,
            @Name("minPeakLatency") int minPeakLatency,
            @Name("maxPeakLatency") int maxPeakLatency) {
        return traceQueryHandler.getAllDBPeakLatency(serviceNames, from, to, minutesAgo, minPeakLatency, maxPeakLatency);
    }



@Query
public List<KafkaMetrics> getKafkaTraceMetricCount(
            @Name("serviceNameList") List<String> serviceName,
            @Name("from") LocalDate from,
            @Name("to") LocalDate to,
            @Name("minutesAgo") int minutesAgo) {

        List<KafkaMetrics> kafkaMetrics;

        kafkaMetrics = traceQueryHandler.getAllKafkaMetrics(serviceName, from, to, minutesAgo);
        return kafkaMetrics;
    }


    @Query
    public List<KafkaMetrics> getKafkaTracePeakLatencyCount(
        @Name("serviceNameList") List<String> serviceName,
        @Name("from") LocalDate from,
        @Name("to") LocalDate to,
        @Name("minutesAgo") int minutesAgo, 
        @Name("minPeakLatency") int minPeakLatency,
        @Name("maxPeakLatency") int maxPeakLatency) {

    List<KafkaMetrics> kafkaMetrics;

    kafkaMetrics = traceQueryHandler.getAllKafkaPeakLatency(serviceName, from, to, minutesAgo, minPeakLatency, maxPeakLatency);
    return kafkaMetrics;
}

@Query
public TracePage sortOrderTrace(
    @Name("sortOrder") String sortOrder,
    @Name("serviceNameList") List<String> serviceNameList,
    @Name("page") int page,
    @Name("pageSize") int pageSize,
    @Name("from") LocalDate fromDate,
    @Name("to") LocalDate toDate,
    @Name("minutesAgo") Integer minutesAgo
) {
    List<TraceDTO> traces;

    if ("new".equalsIgnoreCase(sortOrder)) {
        traces = traceQueryHandler.getAllTracesOrderByCreatedTimeDesc(serviceNameList);
    } else if ("old".equalsIgnoreCase(sortOrder)) {
        traces = traceQueryHandler.getAllTracesAsc(serviceNameList);
    } else if ("error".equalsIgnoreCase(sortOrder)) {
        traces = traceQueryHandler.findAllOrderByErrorFirst(serviceNameList);
    } else if ("peakLatency".equalsIgnoreCase(sortOrder)) {
        traces = traceQueryHandler.findAllOrderByDuration(serviceNameList);
    } else {
        traces = new ArrayList<>();
    }

    Instant fromInstant;
    Instant toInstant;

    if (fromDate != null && toDate != null) {
        fromInstant = fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        toInstant = toDate.atStartOfDay(ZoneId.systemDefault()).toInstant().plus(1, ChronoUnit.DAYS);
    } else if (minutesAgo != null && minutesAgo > 0) {
        Instant currentInstant = Instant.now();
        Instant minutesAgoInstant = currentInstant.minus(minutesAgo, ChronoUnit.MINUTES);

        fromInstant = minutesAgoInstant.isBefore(currentInstant) ? minutesAgoInstant : currentInstant;
        toInstant = currentInstant;
    } else {
        throw new IllegalArgumentException("Either date range or minutesAgo must be provided");
    }

    List<TraceDTO> filteredTraces = traces.stream()
        .filter(trace -> {
            Date createdDate = trace.getCreatedTime();
            if (createdDate != null) {
                Instant createdInstant = createdDate.toInstant();
                return createdInstant.isAfter(fromInstant) && createdInstant.isBefore(toInstant);
            }
            return false;
        })
        .collect(Collectors.toList());

    int totalCount = filteredTraces.size();
    int startIndex = (page - 1) * pageSize;
    int endIndex = Math.min(startIndex + pageSize, filteredTraces.size());

    List<TraceDTO> paginatedTraces;
    if (startIndex < filteredTraces.size()) {
        paginatedTraces = filteredTraces.subList(startIndex, endIndex);
    } else {
        paginatedTraces = new ArrayList<>();
    }

    return new TracePage(paginatedTraces, totalCount);
}



@Query
public TracePage traceFilter(
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
            traceList = traceQueryHandler.getTraceFilterOrderByCreatedTimeDesc(traceQueryHandler.getByFilter(query, fromDate, toDate, minutesAgo));
        } else if ("old".equalsIgnoreCase(sortOrder)) {
            traceList = traceQueryHandler.getTraceFilterAsc(traceQueryHandler.getByFilter(query, fromDate, toDate, minutesAgo));
        } else if ("error".equalsIgnoreCase(sortOrder)) {
            traceList = traceQueryHandler.getTraceFilterOrderByErrorFirst(traceQueryHandler.getByFilter(query, fromDate, toDate, minutesAgo));
        } else if ("peakLatency".equalsIgnoreCase(sortOrder)) {
            traceList = traceQueryHandler.getTraceFilterOrderByDuration(traceQueryHandler.getByFilter(query, fromDate, toDate, minutesAgo));
        }
    } else {
        traceList = traceQueryHandler.getByFilter(query, fromDate, toDate, minutesAgo);
    }

    int totalCount = traceList.size();
    int startIdx = (page - 1) * pageSize;
    int endIdx = Math.min(startIdx + pageSize, traceList.size());
    List<TraceDTO> paginatedTraces = traceList.subList(startIdx, endIdx);

    return new TracePage(paginatedTraces, totalCount);
}



@Query
public  List<TraceSpanDTO>  findByTraceId(@Name("traceId") String traceId) {
        if (traceId == null || traceId.isEmpty()) {
            // Handle bad request, for example, throw an exception or return an empty list
            return Collections.emptyList();
        }
    
        List<TraceDTO> data = traceQueryRepo.find("traceId = ?1", traceId).list();
    
        if (data.isEmpty()) {
            // Handle not found, for example, throw an exception or return an empty list
            return Collections.emptyList();
        }
    
        List<TraceDTO> dto;
        if (data.size() > 1) {
            dto = traceQueryHandler.mergeTraces(data);
        } else {
            dto = data;
            for (TraceDTO trace : dto) {
                List<Spans> orderedSpanData = traceQueryHandler.sortingParentChildOrder(trace.getSpans());
                trace.setSpans(orderedSpanData);
            }
        }
    
        for (TraceDTO trace : dto) {
            for (Spans span : trace.getSpans()) {
                // Uncomment the following lines if you want to print the span details
                // System.out.println(
                //     "Span ID: " + span.getSpanId() + ", Parent Span ID: " + span.getParentSpanId() + ", Name: "
                //             + span.getName());
            }
        }
    
        List<TraceSpanDTO> traceSpanDTO = traceQueryHandler.getModifiedTraceSpanDTO(dto);
    
        // Return the List<TraceSpanDTO> directly if TraceSpanDTO is a subtype of TraceDTO
        return traceSpanDTO;
    }



    @Query
    public List<LogDTO> findByTraceErrorTraceId(@Name("traceId") String traceId) {
    if (traceId == null || traceId.isEmpty()) {
        // Handle bad request, for example, throw an exception or return an empty list
        return Collections.emptyList();
    }

    List<TraceDTO> data = traceQueryRepo.find("traceId = ?1", traceId).list();

    if (data.isEmpty()) {
        // Handle not found, for example, throw an exception or return an empty list
        return Collections.emptyList();
    }

    for (TraceDTO trace : data) {
        for (Spans span : trace.getSpans()) {
            // Uncomment the following lines if you want to print the span details
            // System.out.println(
            //     "Span ID: " + span.getSpanId() + ", Parent Span ID: " + span.getParentSpanId() + ", Name: "
            //             + span.getName());
        }
    }

    List<LogDTO> logDTOs = traceQueryHandler.getErroredLogDTO(data);

    // Return the List<LogDTO> directly
    return logDTOs;
}

}
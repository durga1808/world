package com.graphql.handler.query;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
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
import io.quarkus.panache.common.Page;
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


    
//    public List<TraceDTO> getTracesByStatusCodeAndDuration(TraceQuery query, int page, int pageSize) {
//     PanacheQuery<TraceDTO> panacheQuery = TraceDTO.find(
//             "statusCode >= :minStatusCode and statusCode <= :maxStatusCode " +
//                     "and duration >= :minDuration and duration <= :maxDuration " +
//                     "and serviceName in :serviceNames and methodName in :methodNames",
//             Parameters
//                     .with("minStatusCode", query.getStatusCode().get(0).getMin())
//                     .and("maxStatusCode", query.getStatusCode().get(0).getMax())
//                     .and("minDuration", query.getDuration().getMin())
//                     .and("maxDuration", query.getDuration().getMax())
//                     .and("serviceNames", query.getServiceName())
//                     .and("methodNames", query.getMethodName()));

//     List<TraceDTO> results = panacheQuery.page(Page.of(page, pageSize)).list();

//     return results.stream()
//             .filter(trace ->
//                     trace.getStatusCode() >= query.getStatusCode().get(0).getMin() &&
//                             trace.getStatusCode() <= query.getStatusCode().get(0).getMax() &&
//                             trace.getDuration() >= query.getDuration().getMin() &&
//                             trace.getDuration() <= query.getDuration().getMax() &&
//                             query.getServiceName().contains(trace.getServiceName()) &&
//                             query.getMethodName().contains(trace.getMethodName()))
//             .collect(Collectors.toList());
// }

public List<TraceDTO> getTracesByStatusCodeAndDuration(TraceQuery query, int page, int pageSize, LocalDate fromDate, LocalDate toDate, Integer minutesAgo) {
   
    Instant fromInstant = null;
    Instant toInstant = null;

    if (fromDate != null && toDate != null) {
      Instant startOfFrom = fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
      Instant startOfTo = toDate .atStartOfDay(ZoneId.systemDefault()).toInstant();

      fromInstant = startOfFrom.isBefore(startOfTo) ? startOfFrom : startOfTo;
      toInstant = startOfFrom.isBefore(startOfTo) ? startOfTo : startOfFrom;

      toInstant = toInstant.plus(1, ChronoUnit.DAYS);
    } else if (minutesAgo > 0) {
      Instant currentInstant = Instant.now();
      Instant minutesAgoInstant = currentInstant.minus(minutesAgo, ChronoUnit.MINUTES);

      // Calculate the start of the current day
      Instant startOfCurrentDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();

      if (minutesAgoInstant.isBefore(startOfCurrentDay)) {
        fromInstant = startOfCurrentDay;
      } else {
        fromInstant = minutesAgoInstant;
      }

      toInstant = currentInstant;
    } else {
      throw new IllegalArgumentException("Either date range or minutesAgo must be provided");
    }

    PanacheQuery<TraceDTO> panacheQuery = TraceDTO.find(
            "statusCode >= :minStatusCode and statusCode <= :maxStatusCode " +
                    "and duration >= :minDuration and duration <= :maxDuration " +
                    "and serviceName in :serviceNames and methodName in :methodNames " +
                    "and createdTime >= :fromDate and createdTime <= :toDate",
            Parameters
                    .with("minStatusCode", query.getStatusCode().get(0).getMin())
                    .and("maxStatusCode", query.getStatusCode().get(0).getMax())
                    .and("minDuration", query.getDuration().getMin())
                    .and("maxDuration", query.getDuration().getMax())
                    .and("serviceNames", query.getServiceName())
                    .and("methodNames", query.getMethodName())
                    .and("fromDate", Date.from(fromInstant))
                    .and("toDate", Date.from(toInstant)));

    List<TraceDTO> results = panacheQuery.page(Page.of(page, pageSize)).list();

    return results.stream()
            .filter(trace ->
                    trace.getStatusCode() >= query.getStatusCode().get(0).getMin() &&
                            trace.getStatusCode() <= query.getStatusCode().get(0).getMax() &&
                            trace.getDuration() >= query.getDuration().getMin() &&
                            trace.getDuration() <= query.getDuration().getMax() &&
                            query.getServiceName().contains(trace.getServiceName()) &&
                            query.getMethodName().contains(trace.getMethodName()) &&
                            trace.getCreatedTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().compareTo(fromDate) >= 0 &&
                            trace.getCreatedTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().compareTo(toDate) <= 0)
            .collect(Collectors.toList());
}
}
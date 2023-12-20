package com.graphql.handler.query;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.bson.Document;

import com.graphql.entity.queryentity.trace.TraceDTO;
import com.graphql.entity.queryentity.trace.TraceQuery;
import com.graphql.repo.query.TraceQueryRepo;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;

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

    public List<TraceDTO> getAllTraceData() {
        List<TraceDTO> allTraces = traceQueryRepo.listAll();
        return allTraces;
    }



    public List<TraceDTO> getByServiceName(String serviceName) {
        return traceQueryRepo.findByServiceName(serviceName);
    }

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



// sort order decending
public List<TraceDTO> getAllTracesOrderByCreatedTimeDesc(List<String> serviceNameList) {
  return traceQueryRepo.findAllOrderByCreatedTimeDesc(serviceNameList);
}

// sort order ascending
public List<TraceDTO> getAllTracesAsc(List<String> serviceNameList) {
  return traceQueryRepo.findAllOrderByCreatedTimeAsc(serviceNameList);
}

// sort order error first
public List<TraceDTO> findAllOrderByErrorFirst(List<String> serviceNameList) {
  MongoCollection<Document> traceCollection = mongoClient
      .getDatabase("OtelTrace")
      .getCollection("TraceDTO");

  List<TraceDTO> allTraces = traceCollection.find(TraceDTO.class).into(new ArrayList<>());

  List<TraceDTO> sortedTraces = allTraces.stream()
      .filter(trace -> serviceNameList.contains(trace.getServiceName())) // Filter by service name list
      .sorted(Comparator
          // Sort by error status first (statusCode >= 400 && statusCode <= 599)
          .comparing((TraceDTO trace) -> {
            Long statusCode = trace.getStatusCode();
            return (statusCode != null && statusCode >= 400 && statusCode <= 599) ? 0 : 1;
          })
          // Then sort by status code in descending order
          .thenComparing(TraceDTO::getStatusCode, Comparator.nullsLast(Comparator.reverseOrder()))
          // Finally, sort by created time in descending order
          .thenComparing(TraceDTO::getCreatedTime, Comparator.nullsLast(Comparator.reverseOrder())))
      .collect(Collectors.toList());
  return sortedTraces;
}

//sorting peaked value first
public List<TraceDTO> findAllOrderByDuration(List<String> serviceNameList) {
  MongoCollection<Document> traceCollection = mongoClient
      .getDatabase("OtelTrace")
      .getCollection("TraceDTO");

  List<TraceDTO> allTraces = traceCollection.find(TraceDTO.class).into(new ArrayList<>());

  List<TraceDTO> sortedTraces = allTraces.stream()
      .filter(trace -> serviceNameList.contains(trace.getServiceName()))
      .filter(trace -> trace.getDuration() != null) // Add a null check for duration
      .sorted(Comparator
          .comparing(TraceDTO::getDuration, Comparator.reverseOrder()))
      .collect(Collectors.toList());

  return sortedTraces;
}



//trace filter
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

// filter Sort by created time in descending order
  public List<TraceDTO> getTraceFilterOrderByCreatedTimeDesc(List<TraceDTO> traceList) {
    return traceList.stream()
        .sorted(Comparator.comparing(TraceDTO::getCreatedTime, Comparator.reverseOrder()))
        .collect(Collectors.toList());
  }

  // filter Sort by created time in ascending order
  public List<TraceDTO> getTraceFilterAsc(List<TraceDTO> traceList) {
    return traceList.stream()
        .sorted(Comparator.comparing(TraceDTO::getCreatedTime))
        .collect(Collectors.toList());
  }

  // filter Sort by error first
  public List<TraceDTO> getTraceFilterOrderByErrorFirst(List<TraceDTO> traceList) {
    return traceList.stream()
        .sorted(Comparator
            .comparing(TraceDTO::getStatusCode, Comparator.nullsLast(Comparator.reverseOrder()))
            .thenComparing(TraceDTO::getCreatedTime, Comparator.nullsLast(Comparator.reverseOrder())))
        .collect(Collectors.toList());
  }

    //filter Sort by duration in descending order
    public List<TraceDTO> getTraceFilterOrderByDuration(List<TraceDTO> traceList) {
        return traceList.stream()
            .filter(trace -> trace.getDuration() != null)
            .sorted(Comparator.comparing(TraceDTO::getDuration, Comparator.reverseOrder()))
            .collect(Collectors.toList());
      }
}
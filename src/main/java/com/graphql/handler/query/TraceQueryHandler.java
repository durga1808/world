package com.graphql.handler.query;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.graphql.entity.queryentity.trace.DBMetric;
import com.graphql.entity.queryentity.trace.KafkaMetrics;
import com.graphql.entity.queryentity.trace.TraceDTO;
import com.graphql.entity.queryentity.trace.TraceMetrics;
import com.graphql.entity.queryentity.trace.TraceQuery;
import com.graphql.repo.query.TraceQueryRepo;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;

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

  public List<TraceDTO> getByFilter(TraceQuery query, LocalDate fromDate,  LocalDate toDate,  Integer minutesAgo) {
  Instant fromInstant;
  Instant toInstant;

  if (fromDate != null && toDate != null) {
      Instant startOfFrom = fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
      Instant startOfTo = toDate.atStartOfDay(ZoneId.systemDefault()).toInstant();

      fromInstant = startOfFrom.isBefore(startOfTo) ? startOfFrom : startOfTo;
      toInstant = startOfFrom.isBefore(startOfTo) ? startOfTo : startOfFrom;

      toInstant = toInstant.plus(1, ChronoUnit.DAYS);
  } else if (minutesAgo != null && minutesAgo > 0) {
      Instant currentInstant = Instant.now();
      Instant minutesAgoInstant = currentInstant.minus(minutesAgo, ChronoUnit.MINUTES);

      Instant startOfCurrentDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();

      fromInstant = minutesAgoInstant.isBefore(startOfCurrentDay) ? startOfCurrentDay : minutesAgoInstant;
      toInstant = currentInstant;
  } else {
      throw new IllegalArgumentException("Either date range or minutesAgo must be provided");
  }

  List<Bson> filters = new ArrayList<>();

  if (query.getServiceName() != null) {
      filters.add(Filters.in("serviceName", query.getServiceName()));
  } else if (query.getMethodName() != null) {
      filters.add(Filters.in("methodName", query.getMethodName()));
  }

  filters.add(Filters.gte("createdTime", fromInstant));
  filters.add(Filters.lte("createdTime", toInstant));

  Bson match = Aggregates.match(Filters.and(filters));

  List<TraceDTO> result = TraceDTO.mongoCollection()
          .withDocumentClass(TraceDTO.class)
          .aggregate(Arrays.asList(match), TraceDTO.class)
          .into(new ArrayList<>());

  return result;
}
    
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




public List<TraceDTO> getTracesByStatusCodeAndDuration(TraceQuery query, LocalDate fromDate, LocalDate toDate, Integer minutesAgo) {

    Instant fromInstant = null;
    Instant toInstant = null;

    if (fromDate != null && toDate != null) {
        Instant startOfFrom = fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant startOfTo = toDate.atStartOfDay(ZoneId.systemDefault()).toInstant();

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
                    .with("minStatusCode", Optional.ofNullable(query.getStatusCode())
                            .map(statusCode -> statusCode.get(0).getMin()).orElse(null))
                    .and("maxStatusCode", Optional.ofNullable(query.getStatusCode())
                            .map(statusCode -> statusCode.get(0).getMax()).orElse(null))
                    .and("minDuration", Optional.ofNullable(query.getDuration())
                            .map(duration -> duration.getMin()).orElse(null))
                    .and("maxDuration", Optional.ofNullable(query.getDuration())
                            .map(duration -> duration.getMax()).orElse(null))
                    .and("serviceNames", Optional.ofNullable(query.getServiceName()).orElse(Collections.emptyList()))
                    .and("methodNames", Optional.ofNullable(query.getMethodName()).orElse(Collections.emptyList()))
                    .and("fromDate", Optional.ofNullable(fromInstant).map(Date::from).orElse(null))
                    .and("toDate", Optional.ofNullable(toInstant).map(Date::from).orElse(null)));

    return panacheQuery.list().stream()
            .filter(trace ->
                    trace.getStatusCode() >= Optional.ofNullable(query.getStatusCode())
                            .map(statusCode -> statusCode.get(0).getMin()).orElse((long) 0))
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


//trace summery chart count
public List<TraceMetrics> getAllTraceMetricCount(List<String> serviceNameList, LocalDate fromDate, LocalDate toDate, Integer minutesAgo) {
  List<TraceDTO> traces = fetchTracesByServiceName(serviceNameList);

  Instant fromInstant;
  Instant toInstant;

  if (fromDate != null && toDate != null) {
      fromInstant = fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
      toInstant = toDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
  } else if (minutesAgo != null && minutesAgo > 0) {
      Instant currentInstant = Instant.now();
      fromInstant = currentInstant.minus(minutesAgo, ChronoUnit.MINUTES);
      toInstant = currentInstant;
  } else {
      throw new IllegalArgumentException("Either date range or minutesAgo must be provided");
  }

  return traces.stream()
          .filter(trace -> trace.getCreatedTime() != null && trace.getCreatedTime().toInstant().isAfter(fromInstant) && trace.getCreatedTime().toInstant().isBefore(toInstant))
          .collect(Collectors.groupingBy(TraceDTO::getServiceName))
          .entrySet().stream()
          .map(entry -> {
              String serviceName = entry.getKey();
              List<TraceDTO> serviceTraces = entry.getValue();

              if (serviceTraces != null) {
                  long totalErrorCalls = serviceTraces.stream()
                          .filter(trace -> trace != null && trace.getStatusCode() != null && trace.getStatusCode() >= 400 && trace.getStatusCode() <= 599)
                          .count();

                  long totalSuccessCalls = serviceTraces.stream()
                          .filter(trace -> trace != null && trace.getStatusCode() != null && trace.getStatusCode() >= 200 && trace.getStatusCode() <= 299)
                          .count();

                  long peakLatencyCount = serviceTraces.stream()
                          .filter(trace -> trace != null && trace.getDuration() != null && trace.getDuration() > 500)
                          .count();

                  long apiCallCount = totalErrorCalls + totalSuccessCalls;

                  return new TraceMetrics(serviceName, apiCallCount, peakLatencyCount, totalErrorCalls, totalSuccessCalls);
              } else {
                  // System.out.println("serviceTraces is null for serviceName: " + serviceName);
                  return new TraceMetrics(serviceName, 0L, 0L, 0L, 0L);
              }
          })
          .collect(Collectors.toList());
}

public static List<TraceDTO> fetchTracesByServiceName(List<String> serviceNameList) {
  return TraceDTO.list("serviceName in ?1", serviceNameList);
}



//trace peack latency count
public static List<TraceMetrics> getPeakLatency(List<String> serviceNameList, LocalDate fromDate, LocalDate toDate, Integer minutesAgo, Long minPeakLatency, Long maxPeakLatency) {
  List<TraceDTO> traces = fetchTracesByServiceName(serviceNameList);

  Instant fromInstant;
  Instant toInstant;

  if (fromDate != null && toDate != null) {
      fromInstant = fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
      toInstant = toDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
  } else if (minutesAgo != null && minutesAgo > 0) {
      Instant currentInstant = Instant.now();
      fromInstant = currentInstant.minus(minutesAgo, ChronoUnit.MINUTES);
      toInstant = currentInstant;
  } else {
      throw new IllegalArgumentException("Either date range or minutesAgo must be provided");
  }

  return traces.stream()
          .filter(trace -> trace.getCreatedTime() != null && trace.getCreatedTime().toInstant().isAfter(fromInstant) && trace.getCreatedTime().toInstant().isBefore(toInstant))
          .collect(Collectors.groupingBy(TraceDTO::getServiceName))
          .entrySet().stream()
          .map(entry -> {
              String serviceName = entry.getKey();
              List<TraceDTO> serviceTraces = entry.getValue();

              if (serviceTraces != null) {
                  long totalErrorCalls = serviceTraces.stream()
                          .filter(trace -> trace != null && trace.getStatusCode() != null && trace.getStatusCode() >= 400 && trace.getStatusCode() <= 599)
                          .count();

                  long totalSuccessCalls = serviceTraces.stream()
                          .filter(trace -> trace != null && trace.getStatusCode() != null && trace.getStatusCode() >= 200 && trace.getStatusCode() <= 299)
                          .count();

                  long peakLatencyCount = serviceTraces.stream()
                          .filter(trace -> trace != null && trace.getDuration() != null && trace.getDuration() >= minPeakLatency && trace.getDuration() <= maxPeakLatency)
                          .count();

                          long apiCallCount = serviceTraces.size();
                  return new TraceMetrics(serviceName, apiCallCount, peakLatencyCount, totalErrorCalls, totalSuccessCalls);
              } else {
                  // System.out.println("serviceTraces is null for serviceName: " + serviceName);
                  return new TraceMetrics(serviceName, 0L, 0L, 0L, 0L);
              }
          })
          .collect(Collectors.toList());
}



public List<DBMetric> getAllDBMetrics(List<String> serviceNameList, LocalDate from, LocalDate to, int minutesAgo) {
    MongoCollection<Document> collection = mongoClient.getDatabase("OtelTrace")
        .getCollection("TraceDTO");

    // Match service names
    Bson serviceNameFilter = Filters.in("serviceName", serviceNameList);

    List<Bson> pipeline = new ArrayList<>();

    if (from != null && to != null) {
      // Date-wise filtering
      pipeline.add(Aggregates.match(Filters.and(
          Filters.regex("spans.attributes.key", "^db", "m"),
          serviceNameFilter,
          Filters.gte("createdTime", Date.from(from.atStartOfDay(ZoneId.systemDefault()).toInstant())),
          Filters.lt("createdTime", Date.from(to.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant())))));
    } else if (minutesAgo == 1440) {
      // Fetch data for today
      LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
      LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
      LocalDateTime endOfToday = startOfToday.plusDays(1);

      pipeline.add(Aggregates.match(Filters.and(
          Filters.regex("spans.attributes.key", "^db", "m"),
          serviceNameFilter,
          Filters.gte("createdTime", Date.from(startOfToday.atZone(ZoneId.systemDefault()).toInstant())),
          Filters.lt("createdTime", Date.from(endOfToday.atZone(ZoneId.systemDefault()).toInstant())))));
    } else if (minutesAgo > 0) {
      // Time-based filtering
      LocalDateTime thresholdTime = LocalDateTime.now().minusMinutes(minutesAgo);

      pipeline.add(Aggregates.match(Filters.and(
          Filters.regex("spans.attributes.key", "^db", "m"),
          serviceNameFilter,
          Filters.gte("createdTime", Date.from(thresholdTime.atZone(ZoneId.systemDefault()).toInstant())))));
    }

    pipeline.add(Aggregates.unwind("$spans"));
    pipeline.add(Aggregates.match(Filters.and(
        Filters.in("serviceName", serviceNameList),
        Filters.regex("spans.attributes.key", "^db", "m"))));
    pipeline.add(Aggregates.project(Projections.fields(
        Projections.computed("serviceName", "$serviceName"),
        Projections.computed("startTimeUnixNano", "$spans.startTimeUnixNano"),
        Projections.computed("endTimeUnixNano", "$spans.endTimeUnixNano"))));

    AggregateIterable<Document> result = collection.aggregate(pipeline);

    Map<String, DBMetric> dbMetricMap = new HashMap<>();

    result.forEach((Consumer<? super Document>) document -> {
      String serviceName = getAsStringOrFallback(document, "serviceName", "Unknown");

      String startTimeUnixNanoStr = document.getString("startTimeUnixNano");
      String endTimeUnixNanoStr = document.getString("endTimeUnixNano");

      long startTimeUnixNano = Long.parseLong(startTimeUnixNanoStr);
      long endTimeUnixNano = Long.parseLong(endTimeUnixNanoStr);

      ZonedDateTime startIST = Instant.ofEpochSecond(0, startTimeUnixNano).atZone(ZoneId.of("Asia/Kolkata"));
      ZonedDateTime endIST = Instant.ofEpochSecond(0, endTimeUnixNano).atZone(ZoneId.of("Asia/Kolkata"));
      long dbduration = ChronoUnit.MILLIS.between(startIST, endIST);

      String key = serviceName;
      DBMetric dbMetric = dbMetricMap.computeIfAbsent(key, k -> new DBMetric(serviceName, 0L, 0L));

      dbMetric.setDbCallCount(dbMetric.getDbCallCount() + 1);
      if (dbduration > 50) {
        dbMetric.setDbPeakLatencyCount(Math.max(dbMetric.getDbPeakLatencyCount(), dbduration));
      }
    });

    List<DBMetric> resultList = new ArrayList<>(dbMetricMap.values());

    return resultList;
  }



  private String getAsStringOrFallback(Document document, String fieldName, String fallback) {
    return document.containsKey(fieldName) ? document.getString(fieldName) : fallback;
}



public List<DBMetric> getAllDBPeakLatency(List<String> serviceNameList, LocalDate from, LocalDate to,
      int minutesAgo, int minPeakLatency, int maxPeakLatency) {
    MongoCollection<Document> collection = mongoClient.getDatabase("OtelTrace")
        .getCollection("TraceDTO");

    // Match service names
    Bson serviceNameFilter = Filters.in("serviceName", serviceNameList);

    List<Bson> pipeline = new ArrayList<>();

    if (from != null && to != null) {
      // Date-wise filtering
      pipeline.add(Aggregates.match(Filters.and(
          Filters.regex("spans.attributes.key", "^db", "m"),
          serviceNameFilter,
          Filters.gte("createdTime", Date.from(from.atStartOfDay(ZoneId.systemDefault()).toInstant())),
          Filters.lt("createdTime", Date.from(to.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant())))));
    } else if (minutesAgo == 1440) {
      // Fetch data for today
      LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
      LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
      LocalDateTime endOfToday = startOfToday.plusDays(1);

      pipeline.add(Aggregates.match(Filters.and(
          Filters.regex("spans.attributes.key", "^db", "m"),
          serviceNameFilter,
          Filters.gte("createdTime", Date.from(startOfToday.atZone(ZoneId.systemDefault()).toInstant())),
          Filters.lt("createdTime", Date.from(endOfToday.atZone(ZoneId.systemDefault()).toInstant())))));
    } else if (minutesAgo > 0) {
      // Time-based filtering
      LocalDateTime thresholdTime = LocalDateTime.now().minusMinutes(minutesAgo);

      pipeline.add(Aggregates.match(Filters.and(
          Filters.regex("spans.attributes.key", "^db", "m"),
          serviceNameFilter,
          Filters.gte("createdTime", Date.from(thresholdTime.atZone(ZoneId.systemDefault()).toInstant())))));
    }

    pipeline.add(Aggregates.unwind("$spans"));
    pipeline.add(Aggregates.match(Filters.and(
        Filters.in("serviceName", serviceNameList),
        Filters.regex("spans.attributes.key", "^db", "m"))));
    pipeline.add(Aggregates.project(Projections.fields(
        Projections.computed("serviceName", "$serviceName"),
        Projections.computed("startTimeUnixNano", "$spans.startTimeUnixNano"),
        Projections.computed("endTimeUnixNano", "$spans.endTimeUnixNano"))));

    AggregateIterable<Document> result = collection.aggregate(pipeline);

    Map<String, DBMetric> dbMetricMap = new HashMap<>();

    result.forEach((Consumer<? super Document>) document -> {
      String serviceName = getAsStringOrFallback(document, "serviceName", "Unknown");

      String startTimeUnixNanoStr = document.getString("startTimeUnixNano");
      String endTimeUnixNanoStr = document.getString("endTimeUnixNano");

      long startTimeUnixNano = Long.parseLong(startTimeUnixNanoStr);
      long endTimeUnixNano = Long.parseLong(endTimeUnixNanoStr);

      ZonedDateTime startIST = Instant.ofEpochSecond(0, startTimeUnixNano).atZone(ZoneId.of("Asia/Kolkata"));
      ZonedDateTime endIST = Instant.ofEpochSecond(0, endTimeUnixNano).atZone(ZoneId.of("Asia/Kolkata"));
      long dbduration = ChronoUnit.MILLIS.between(startIST, endIST);



      String key = serviceName;
      DBMetric dbMetric = dbMetricMap.computeIfAbsent(key, k -> new DBMetric(serviceName, 0L, 0L));

      
     if (dbduration >= minPeakLatency && dbduration <= maxPeakLatency) { 
        // Update the count based on the maximum dbDuration
        dbMetric.setDbPeakLatencyCount(Math.max(dbMetric.getDbPeakLatencyCount(), dbduration));
    }


     });
     

    List<DBMetric> resultList = new ArrayList<>(dbMetricMap.values());

    return resultList;
  }



public List<KafkaMetrics> getAllKafkaMetrics(List<String> serviceNames, LocalDate from, LocalDate to,
      int minutesAgo) {
    MongoCollection<Document> collection = mongoClient.getDatabase("OtelTrace")
        .getCollection("TraceDTO");

    List<Bson> pipeline = new ArrayList<>();
    LocalDateTime currentTime = LocalDateTime.now();

    if (from != null && to != null) {
      // Date-wise filtering
      ZonedDateTime fromZoned = from.atStartOfDay(ZoneId.systemDefault()).toInstant().atZone(ZoneId.systemDefault());
      ZonedDateTime toZoned = to.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()
          .atZone(ZoneId.systemDefault());

      pipeline.add(Aggregates.match(Filters.and(
          Filters.regex("spans.attributes.key", "^messaging", "m"),
          Filters.in("serviceName", serviceNames),
          Filters.gte("createdTime", Date.from(fromZoned.toInstant())),
          Filters.lt("createdTime", Date.from(toZoned.toInstant())))));
    } else if (minutesAgo > 0) {
      // Time-based filtering
      LocalDateTime thresholdTime = currentTime.minusMinutes(minutesAgo);
      ZonedDateTime thresholdZoned = thresholdTime.atZone(ZoneId.systemDefault());

      pipeline.add(Aggregates.match(Filters.and(
          Filters.regex("spans.attributes.key", "^messaging", "m"),
          Filters.in("serviceName", serviceNames),
          Filters.gte("createdTime", Date.from(thresholdZoned.toInstant())))));
    }

    pipeline.add(Aggregates.unwind("$spans"));
    pipeline.add(Aggregates.match(Filters.and(
        Filters.in("serviceName", serviceNames),
        Filters.regex("spans.attributes.key", "^messaging", "m"))));
    pipeline.add(Aggregates.project(Projections.fields(
        Projections.computed("serviceName", "$serviceName"),
        Projections.computed("startTimeUnixNano", "$spans.startTimeUnixNano"),
        Projections.computed("endTimeUnixNano", "$spans.endTimeUnixNano"))));

    AggregateIterable<Document> result = collection.aggregate(pipeline);

    Map<String, KafkaMetrics> kafkaMetricsMap = new HashMap<>();

    result.forEach((Consumer<? super Document>) document -> {
      String serviceName = getAsStringOrFallback(document, "serviceName", "Unknown");

      String startTimeUnixNanoStr = document.getString("startTimeUnixNano");
      String endTimeUnixNanoStr = document.getString("endTimeUnixNano");

      long startTimeUnixNano = Long.parseLong(startTimeUnixNanoStr);
      long endTimeUnixNano = Long.parseLong(endTimeUnixNanoStr);

      ZonedDateTime startIST = Instant.ofEpochSecond(0, startTimeUnixNano).atZone(ZoneId.of("Asia/Kolkata"));
      ZonedDateTime endIST = Instant.ofEpochSecond(0, endTimeUnixNano).atZone(ZoneId.of("Asia/Kolkata"));
      long kafkaDuration = ChronoUnit.MILLIS.between(startIST, endIST);

      String key = serviceName;
      KafkaMetrics kafkaMetrics = kafkaMetricsMap.computeIfAbsent(key, k -> new KafkaMetrics(serviceName, 0L, 0L));

      kafkaMetrics.setKafkaCallCount(kafkaMetrics.getKafkaCallCount() + 1);
      if (kafkaDuration > 5) {
        kafkaMetrics.setKafkaPeakLatency(kafkaMetrics.getKafkaPeakLatency() + 1);
      }
    });

    List<KafkaMetrics> resultList = new ArrayList<>(kafkaMetricsMap.values());

    return resultList;
  }

  public List<KafkaMetrics> getAllKafkaPeakLatency(List<String> serviceNames, LocalDate from, LocalDate to,
      int minutesAgo, int minPeakLatency, int maxPeakLatency) {
    MongoCollection<Document> collection = mongoClient.getDatabase("OtelTrace")
        .getCollection("TraceDTO");

    List<Bson> pipeline = new ArrayList<>();
    LocalDateTime currentTime = LocalDateTime.now();

    if (from != null && to != null) {
      // Date-wise filtering
      ZonedDateTime fromZoned = from.atStartOfDay(ZoneId.systemDefault()).toInstant().atZone(ZoneId.systemDefault());
      ZonedDateTime toZoned = to.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()
          .atZone(ZoneId.systemDefault());

      pipeline.add(Aggregates.match(Filters.and(
          Filters.regex("spans.attributes.key", "^messaging", "m"),
          Filters.in("serviceName", serviceNames),
          Filters.gte("createdTime", Date.from(fromZoned.toInstant())),
          Filters.lt("createdTime", Date.from(toZoned.toInstant())))));
    } else if (minutesAgo > 0) {
      // Time-based filtering
      LocalDateTime thresholdTime = currentTime.minusMinutes(minutesAgo);
      ZonedDateTime thresholdZoned = thresholdTime.atZone(ZoneId.systemDefault());

      pipeline.add(Aggregates.match(Filters.and(
          Filters.regex("spans.attributes.key", "^messaging", "m"),
          Filters.in("serviceName", serviceNames),
          Filters.gte("createdTime", Date.from(thresholdZoned.toInstant())))));
    }

    pipeline.add(Aggregates.unwind("$spans"));
    pipeline.add(Aggregates.match(Filters.and(
        Filters.in("serviceName", serviceNames),
        Filters.regex("spans.attributes.key", "^messaging", "m"))));
    pipeline.add(Aggregates.project(Projections.fields(
        Projections.computed("serviceName", "$serviceName"),
        Projections.computed("startTimeUnixNano", "$spans.startTimeUnixNano"),
        Projections.computed("endTimeUnixNano", "$spans.endTimeUnixNano"))));

    AggregateIterable<Document> result = collection.aggregate(pipeline);

    Map<String, KafkaMetrics> kafkaMetricsMap = new HashMap<>();

    result.forEach((Consumer<? super Document>) document -> {
      String serviceName = getAsStringOrFallback(document, "serviceName", "Unknown");

      String startTimeUnixNanoStr = document.getString("startTimeUnixNano");
      String endTimeUnixNanoStr = document.getString("endTimeUnixNano");

      long startTimeUnixNano = Long.parseLong(startTimeUnixNanoStr);
      long endTimeUnixNano = Long.parseLong(endTimeUnixNanoStr);

      ZonedDateTime startIST = Instant.ofEpochSecond(0, startTimeUnixNano).atZone(ZoneId.of("Asia/Kolkata"));
      ZonedDateTime endIST = Instant.ofEpochSecond(0, endTimeUnixNano).atZone(ZoneId.of("Asia/Kolkata"));
      long kafkaDuration = ChronoUnit.MILLIS.between(startIST, endIST);

      String key = serviceName;
      KafkaMetrics kafkaMetrics = kafkaMetricsMap.computeIfAbsent(key, k -> new KafkaMetrics(serviceName, 0L, 0L));

      // kafkaMetrics.setKafkaCallCount(kafkaMetrics.getKafkaCallCount() + 1);
      // if (kafkaDuration > peakLatency) {
      //   kafkaMetrics.setKafkaPeakLatency(kafkaMetrics.getKafkaPeakLatency() + 1);
      // }
      if (kafkaDuration >= minPeakLatency && kafkaDuration <= maxPeakLatency) {
        kafkaMetrics.setKafkaPeakLatency(kafkaMetrics.getKafkaPeakLatency() + 1);
      }
    });

    List<KafkaMetrics> resultList = new ArrayList<>(kafkaMetricsMap.values());

    return resultList;
  }


}
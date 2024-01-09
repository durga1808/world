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
import java.util.stream.LongStream;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.graphql.entity.otellog.ScopeLogs;
import com.graphql.entity.otellog.scopeLogs.LogRecord;
import com.graphql.entity.otellog.scopeLogs.logRecord.LogAttribute;
import com.graphql.entity.oteltrace.scopeSpans.Spans;
import com.graphql.entity.queryentity.log.LogDTO;
import com.graphql.entity.queryentity.trace.DBMetric;
import com.graphql.entity.queryentity.trace.KafkaMetrics;
import com.graphql.entity.queryentity.trace.SpanDTO;
import com.graphql.entity.queryentity.trace.TraceDTO;
import com.graphql.entity.queryentity.trace.TraceMetrics;
import com.graphql.entity.queryentity.trace.TraceQuery;
import com.graphql.entity.queryentity.trace.TraceSpanDTO;
import com.graphql.repo.query.LogQueryRepo;
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
  LogQueryRepo logQueryRepo;
    
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

 if (query.getStatusCode() != null && !query.getStatusCode().isEmpty()) {
        List<Long> statusCodeList = query.getStatusCode().stream()
                .flatMap(range -> LongStream.rangeClosed(range.getMin(), range.getMax()).boxed())
                .collect(Collectors.toList());

        filters.add(Filters.in("statusCode", statusCodeList));
    }

  if (query.getDuration() != null && query.getDuration().getMin() != null && query.getDuration().getMax() != null) {
    filters.add(Filters.gte("duration", query.getDuration().getMin()));
    filters.add(Filters.lte("duration", query.getDuration().getMax()));
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



  // Method to merge spans with the same traceId

  public List<TraceDTO> mergeTraces(List<TraceDTO> traces) {
    Map<String, TraceDTO> parentTraces = new HashMap<>();
    Map<String, TraceDTO> childTraces = new HashMap<>();

    for (TraceDTO trace : traces) {
      String traceId = trace.getTraceId();
      boolean hasNullParentSpan = false;

      for (Spans span : trace.getSpans()) {
        if (span.getParentSpanId() == null || span.getParentSpanId().isEmpty()) {
          hasNullParentSpan = true;
          break;
        }
      }

      if (hasNullParentSpan) {
        parentTraces.put(traceId, trace);
      } else {
        childTraces.put(traceId, trace);
      }
    }

    for (TraceDTO parentTrace : parentTraces.values()) {
      String traceId = parentTrace.getTraceId();
      TraceDTO childTrace = childTraces.get(traceId);

      if (childTrace != null) {
        // Merge the spans of the child trace into the parent trace
        parentTrace.getSpans().addAll(childTrace.getSpans());
      }
    }

    // Sort the spans within each merged trace
    for (TraceDTO mergedTrace : parentTraces.values()) {
      mergedTrace.setSpans(sortingParentChildOrder(mergedTrace.getSpans()));
    }

    return new ArrayList<>(parentTraces.values());
  }

    // getByTraceId sort the spans and if some traceId Has same value it will merge
  // the value
  public List<Spans> sortingParentChildOrder(List<Spans> spanData) {
    Map<String, List<Spans>> spanTree = new HashMap<>();

    List<Spans> rootSpans = new ArrayList<>();

    for (Spans span : spanData) {
      // String spanId = span.getSpanId();
      String parentId = span.getParentSpanId();
      if (parentId == null || parentId.isEmpty()) {
        rootSpans.add(span);
      } else {
        spanTree.computeIfAbsent(parentId, k -> new ArrayList<>()).add(span);
      }
    }

    List<Spans> sortedSpans = new ArrayList<>();

    for (Spans rootSpan : rootSpans) {
      sortSpans(rootSpan, spanTree, sortedSpans);
    }

    return sortedSpans;
  }

  private void sortSpans(Spans span, Map<String, List<Spans>> spanTree, List<Spans> sortedSpans) {
    sortedSpans.add(span);
    List<Spans> childSpans = spanTree.get(span.getSpanId());
    if (childSpans != null) {
      for (Spans childSpan : childSpans) {
        sortSpans(childSpan, spanTree, sortedSpans);
      }
    }
  }



   public List<TraceSpanDTO> getModifiedTraceSpanDTO(List<TraceDTO> mergedTraces) {
    List<TraceSpanDTO> traceSpanDTOList = new ArrayList<>();

    for (TraceDTO trace : mergedTraces) {
      String traceID = trace.getTraceId();
      List<Spans> spans = trace.getSpans();

      List<SpanDTO> spanDTOList = new ArrayList<>();

      for (Spans span : spans) {
        SpanDTO spanDTO = new SpanDTO();
        spanDTO.setSpans(span);

        List<LogDTO> logDTOs = fetchLogDTOsForSpanId(span.getSpanId());

        // Filter the LogDTO objects with severityText "ERROR" or "SEVERE"
        List<LogDTO> matchingLogDTOs = logDTOs.stream()
            .filter(logDTO -> "ERROR".equals(logDTO.getSeverityText()) || "SEVERE".equals(logDTO.getSeverityText()))
            .collect(Collectors.toList());

        if (!matchingLogDTOs.isEmpty()) {
          spanDTO.setErrorStatus(true);
          spanDTO.setLogAttributes(matchingLogDTOs.stream()
              .flatMap(logDTO -> extractLogAttributes(logDTO).stream())
              .collect(Collectors.toList()));

          // Set traceId and spanId from the first matching LogRecord (assuming there's at
          // least one)
          LogRecord firstMatchingLogRecord = matchingLogDTOs.get(0).getScopeLogs().get(0).getLogRecords().get(0);
          spanDTO.setLogTraceId(firstMatchingLogRecord.getTraceId());
          spanDTO.setLogSpanId(firstMatchingLogRecord.getSpanId());
          spanDTO.setErrorMessage(firstMatchingLogRecord.getBody()); // Assuming Body is a string
        }

        spanDTOList.add(spanDTO);
      }

      // Create a new TraceSpanDTO with the same properties but using the modified
      // spanDTOList
      TraceSpanDTO traceSpanDTO = new TraceSpanDTO();

      traceSpanDTO.setTraceId(traceID);
      traceSpanDTO.setServiceName(trace.getServiceName());
      traceSpanDTO.setMethodName(trace.getMethodName());
      traceSpanDTO.setOperationName(trace.getOperationName());
      traceSpanDTO.setDuration(trace.getDuration());
      traceSpanDTO.setStatusCode(trace.getStatusCode());
      traceSpanDTO.setSpanCount(trace.getSpanCount());
      traceSpanDTO.setCreatedTime(trace.getCreatedTime());
      traceSpanDTO.setSpanDTOs(spanDTOList);

      traceSpanDTOList.add(traceSpanDTO);
    }

    return traceSpanDTOList;
  }


  public List<LogDTO> fetchLogDTOsForSpanId(String spanId) {
    PanacheQuery<LogDTO> query = logQueryRepo.find("spanId", spanId);
    List<LogDTO> logDTOs = query.list();

    return logDTOs;
  }

  private List<LogAttribute> extractLogAttributes(LogDTO logDTO) {
    List<LogAttribute> logAttributes = new ArrayList<>();

    List<ScopeLogs> scopeLogs = logDTO.getScopeLogs();
    if (scopeLogs != null) {
      for (ScopeLogs scopeLog : scopeLogs) {
        List<LogRecord> logRecords = scopeLog.getLogRecords();
        if (logRecords != null) {
          for (LogRecord logRecord : logRecords) {
            // Extract the LogAttributes from the LogRecord's attributes list
            List<LogAttribute> attributes = logRecord.getAttributes();
            if (attributes != null) {
              logAttributes.addAll(attributes);
            }
          }
        }
      }
    }

    return logAttributes;
  }



  public List<LogDTO> getErroredLogDTO(List<TraceDTO> mergedTraces) {

    List<LogDTO> matchingLogDTOList = new ArrayList<>();
    for (TraceDTO trace : mergedTraces) {
      String traceID = trace.getTraceId();
      List<Spans> spans = trace.getSpans();
      List<LogDTO> logDTOList = logQueryRepo.find("traceId", traceID).list();

      for (LogDTO logDTO : logDTOList) {
        for (Spans span : spans) {
          if (logDTO.getSpanId().equals(span.getSpanId())) {
            // Add the matching LogDTO to the list
            matchingLogDTOList.add(logDTO);
          }
        }

      }
    }
    List<LogDTO> filteredLogDTOList = matchingLogDTOList.stream()
        .filter(logDTO -> "ERROR".equals(logDTO.getSeverityText()) || "SEVERE".equals(logDTO.getSeverityText()))
        .collect(Collectors.toList());

    return filteredLogDTOList;
  }


}
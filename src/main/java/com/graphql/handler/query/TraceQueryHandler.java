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


    //filter query method
  private Bson createCustomDateFilter(LocalDate from, LocalDate to) {
    return Filters.and(
        Filters.gte("createdTime", from.atStartOfDay()),
        Filters.lt("createdTime", to.plusDays(1).atStartOfDay()));
  }

  private FindIterable<Document> getFilteredResults(TraceQuery query, int page, int pageSize, LocalDate from,
      LocalDate to, int minutesAgo) {
    List<Bson> filters = new ArrayList<>();

    if (from != null && to != null) {
      Bson timeFilter = createCustomDateFilter(from, to);
      filters.add(timeFilter);
    } else if (minutesAgo > 0) {
      LocalDate currentDate = LocalDate.now();

      if (from != null && from.isEqual(currentDate)) {
        // If the date is the current date, apply time filter based on minutes ago
        long currentTimeInMillis = System.currentTimeMillis();
        long timeAgoInMillis = currentTimeInMillis - (minutesAgo * 60 * 1000);

        // Ensure that the time filter doesn't go beyond the current day
        long startOfDayMillis = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        if (timeAgoInMillis < startOfDayMillis) {
          timeAgoInMillis = startOfDayMillis;
        }

        Bson timeFilter = Filters.gte("createdTime", new Date(timeAgoInMillis));
        filters.add(timeFilter);
      } else if (from != null) {
        // If a specific date is provided, use it for filtering
        Bson timeFilter = createCustomDateFilter(from, from);
        filters.add(timeFilter);
      }
    }

    if (query.getMethodName() != null && !query.getMethodName().isEmpty()) {
      Bson methodNameFilter = Filters.in("methodName", query.getMethodName());
      filters.add(methodNameFilter);
    }

    if (query.getServiceName() != null && !query.getServiceName().isEmpty()) {
      Bson serviceNameFilter = Filters.in("serviceName", query.getServiceName());
      filters.add(serviceNameFilter);
    }

    if (query.getDuration() != null) {
      Bson durationFilter = Filters.and(
          Filters.gte("duration", query.getDuration().getMin()),
          Filters.lte("duration", query.getDuration().getMax()));
      filters.add(durationFilter);
    }

    List<Bson> statusCodeFilters = new ArrayList<>();
    if (query.getStatusCode() != null && !query.getStatusCode().isEmpty()) {
      for (StatusCodeRange statusCodeRange : query.getStatusCode()) {
        statusCodeFilters.add(
            Filters.and(
                Filters.gte("statusCode", statusCodeRange.getMin()),
                Filters.lte("statusCode", statusCodeRange.getMax())));
      }
    }

    if (!statusCodeFilters.isEmpty()) {
      Bson statusCodeFilter = Filters.or(statusCodeFilters);
      filters.add(statusCodeFilter);
    }

    Bson filter = Filters.and(filters);

    MongoCollection<Document> collection = mongoClient
        .getDatabase("OtelTrace")
        .getCollection("TraceDTO");

    Bson projection = Projections.excludeId();

    System.out.println("Skip: " + (page - 1) * pageSize);
    System.out.println("Limit: " + pageSize);

    Bson sort = Sorts.descending("createdTime");

    return collection
        .find(filter)
        .projection(projection)
        .sort(sort)
        .skip((page - 1) * pageSize)
        .limit(pageSize);
  }

  // getTrace by multiple queries like serviceName, method, duration and
  // statuscode from TraceDTO entity
  public List<TraceDTO> searchTracesPaged(TraceQuery query, int page, int pageSize, LocalDate from, LocalDate to,
  Integer minutesAgo) {
    System.out.println("from Date --------------" + from);
    System.out.println("to Date --------------" + to);

    // Swap 'from' and 'to' if 'to' is earlier than 'from'
    if (from != null && to != null && to.isBefore(from)) {
      LocalDate temp = from;
      from = to;
      to = temp;
    }

    FindIterable<Document> result = getFilteredResults(query, page, pageSize, from, to, minutesAgo);

    List<TraceDTO> traceDTOList = new ArrayList<>();
    try (MongoCursor<Document> cursor = result.iterator()) {
      while (cursor.hasNext()) {
        Document document = cursor.next();
        TraceDTO traceDTO = new TraceDTO();

        traceDTO.setTraceId(document.getString("traceId"));
        traceDTO.setServiceName(document.getString("serviceName"));
        Object durationObject = document.get("duration");
        if (durationObject instanceof Integer) {
          traceDTO.setDuration(((Integer) durationObject).longValue());
        } else if (durationObject instanceof Long) {
          traceDTO.setDuration((Long) durationObject);
        }

        Object statusCodeObject = document.get("statusCode");
        if (statusCodeObject instanceof Integer) {
          traceDTO.setStatusCode(((Integer) statusCodeObject).longValue());
        } else if (statusCodeObject instanceof Long) {
          traceDTO.setStatusCode((Long) statusCodeObject);
        }
        traceDTO.setMethodName(document.getString("methodName"));
        traceDTO.setOperationName(document.getString("operationName"));
        traceDTO.setSpanCount(document.getString("spanCount"));
        traceDTO.setCreatedTime(document.getDate("createdTime"));
        List<Spans> spansList = (List<Spans>) document.get("spans");
        traceDTO.setSpans(spansList);

        traceDTOList.add(traceDTO);
      }
    }

    return traceDTOList;
  }

  public long countQueryTraces(TraceQuery query, LocalDate from, LocalDate to, int minutesAgo) {
    // Swap 'from' and 'to' if 'to' is earlier than 'from'
    if (from != null && to != null && to.isBefore(from)) {
      LocalDate temp = from;
      from = to;
      to = temp;
    }

    FindIterable<Document> result = getFilteredResults(query, 0, Integer.MAX_VALUE, from, to, minutesAgo);
    System.out.println("countQueryTraces: " + result.into(new ArrayList<>()).size());
    long totalCount = result.into(new ArrayList<>()).size();
    return totalCount;
  }

}
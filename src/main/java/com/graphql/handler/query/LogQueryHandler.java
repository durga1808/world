package com.graphql.handler.query;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.graphql.entity.otellog.ScopeLogs;
import com.graphql.entity.otellog.scopeLogs.LogRecord;
import com.graphql.entity.queryentity.log.LogDTO;
import com.graphql.entity.queryentity.log.LogMetrics;
import com.graphql.entity.queryentity.log.LogQuery;
import com.graphql.repo.query.LogQueryRepo;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Field;
import com.mongodb.client.model.Filters;

import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;



@ApplicationScoped
public class LogQueryHandler {
     



    @Inject
     LogQueryRepo logQueryRepo;



     @Inject
     MongoClient mongoClient;


    public List<LogDTO> getAllLogs(){
        List<LogDTO> loglList =logQueryRepo.listAll();
        return loglList;
    }
   
    // public List<LogDTO> getlogByServiceName(String serviceName) {
    //     return logQueryRepo.findByServiceName(serviceName);
    // }







// //From date and To date and minutesago is anyone is optional
// public List<LogDTO> searchLogsPaged(LogQuery logQuery, LocalDate fromDate, LocalDate toDate, Integer minutesAgo) {
//     List<String> serviceNames = logQuery.getServiceName();
//     List<String> severityTexts = logQuery.getSeverityText();

//     List<LogDTO> logList = logQueryRepo.listAll();

//     Instant toInstant = toDate != null ? toDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant() : Instant.now();
//     Instant fromInstant;

//     if (fromDate != null) {
//         fromInstant = fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
//     } else if (minutesAgo != null) {
//         fromInstant = toInstant.minus(minutesAgo, ChronoUnit.MINUTES);
//     } else {
       
//         fromInstant = Instant.EPOCH;
//     }

//     List<LogDTO> filteredAndSortedLogs = logList.stream()
//             .filter(logDTO -> (serviceNames == null || serviceNames.isEmpty() || serviceNames.contains(logDTO.getServiceName())) &&
//                     (severityTexts == null || severityTexts.isEmpty() || severityTexts.contains(logDTO.getSeverityText())))
//             .filter(logDTO -> isWithinDateRange(logDTO.getCreatedTime().toInstant(), fromInstant, toInstant))
//             .collect(Collectors.toList());

//     filteredAndSortedLogs.sort(Comparator.comparing(LogDTO::getCreatedTime).reversed());

//     return filteredAndSortedLogs;
// }

// private boolean isWithinDateRange(Instant logTimestamp, Instant from, Instant to) {
//     return !logTimestamp.isBefore(from) && !logTimestamp.isAfter(to);
// }

// public List<LogDTO> getFilterLogsByCreatedTimeDesc(List<LogDTO> logs) {
//     System.out.println("------getFilterLogsByCreatedTimeDesc---------"+logs.size());
//     return logs.stream()
// .sorted(Comparator.comparing(LogDTO::getCreatedTime, Comparator.reverseOrder()))
// .collect(Collectors.toList());

// }


// public List<LogDTO> getFilterLogssAsc(List<LogDTO> logs) {
//    return logs.stream().sorted(Comparator.comparing(LogDTO::getCreatedTime)).collect(Collectors.toList());
// }

// public List<LogDTO> getFilterErrorLogs(List<LogDTO> logs) {
//         return logs.stream()
//             .sorted(Comparator
//                 .comparing((LogDTO log) -> {
//                     String severityText = log.getSeverityText();
//                     return ("ERROR".equals(severityText) || "SEVERE".equals(severityText)) ? 0 : 1;
//                 })
//                 .thenComparing(LogDTO::getCreatedTime, Comparator.nullsLast(Comparator.reverseOrder()))
//             )
//             .collect(Collectors.toList());
//     }
   


// //LogSummaryChartCount



//     public List<LogMetrics> getLogMetricCount( List<String> serviceNameList, LocalDate from, LocalDate to,Integer minutesAgo
            
//     ) {
//         System.out.println("from: " + from);
//         System.out.println("to: " + to);
//         System.out.println("minutesAgo: " + minutesAgo);

//         List<LogDTO> logList = logQueryRepo.listAll();
//         Map<String, LogMetrics> metricsMap = new HashMap<>();

//         Instant fromInstant = null;
//         Instant toInstant = null;

//         if (from != null && to != null) {
//             Instant startOfFrom = from.atStartOfDay(ZoneId.systemDefault()).toInstant();
//             Instant startOfTo = to.atStartOfDay(ZoneId.systemDefault()).toInstant();

//             fromInstant = startOfFrom.isBefore(startOfTo) ? startOfFrom : startOfTo;
//             toInstant = startOfFrom.isBefore(startOfTo) ? startOfTo : startOfFrom;

//             toInstant = toInstant.plus(1, ChronoUnit.DAYS);
//         } else if (minutesAgo > 0) {
//             Instant currentInstant = Instant.now();
//             Instant minutesAgoInstant = currentInstant.minus(minutesAgo, ChronoUnit.MINUTES);

//             Instant startOfCurrentDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();

//             if (minutesAgoInstant.isBefore(startOfCurrentDay)) {
//                 fromInstant = startOfCurrentDay;
//             } else {
//                 fromInstant = minutesAgoInstant;
//             }

//             toInstant = currentInstant;
//         } else {
//             throw new IllegalArgumentException("Either date range or minutesAgo must be provided");
//         }

//         for (LogDTO logDTO : logList) {
//             Date logCreateTime = logDTO.getCreatedTime();
//             if (logCreateTime != null) {
//                 Instant logInstant = logCreateTime.toInstant();

//                 if (logInstant.isAfter(fromInstant) && logInstant.isBefore(toInstant)
//                         && serviceNameList.contains(logDTO.getServiceName())) {
//                     String serviceName = logDTO.getServiceName();
//                     LogMetrics metrics = metricsMap.get(serviceName);

//                     if (metrics == null) {
//                         metrics = new LogMetrics();
//                         metrics.setServiceName(serviceName);
//                         metrics.setErrorCallCount(0L);
//                         metrics.setWarnCallCount(0L);
//                         metrics.setDebugCallCount(0L);
//                         metricsMap.put(serviceName, metrics);
//                     }

//                     calculateCallCounts(logDTO, metrics);
//                 }
//             }
//         }

//         return new ArrayList<>(metricsMap.values());
//     }

//     private void calculateCallCounts(LogDTO logDTO, LogMetrics metrics) {
//         for (ScopeLogs scopeLogs : logDTO.getScopeLogs()) {
//             for (LogRecord logRecord : scopeLogs.getLogRecords()) {
//                 String severityText = logRecord.getSeverityText(); 
//                 if ("ERROR".equals(severityText) || "SEVERE".equals(severityText)) {
//                     metrics.setErrorCallCount(metrics.getErrorCallCount() + 1);
//                 } else if ("WARN".equals(severityText)) {
//                     metrics.setWarnCallCount(metrics.getWarnCallCount() + 1);
//                 } else if ("DEBUG".equals(severityText)) {
//                     metrics.setDebugCallCount(metrics.getDebugCallCount() + 1);
//                 }
//             }
//         }
//     }




//     public List<LogDTO> getAllLogssOrderByCreatedTimeDesc(List<String> serviceNameList) {
//         return logQueryRepo.findAllOrderByCreatedTimeDesc(serviceNameList);
//     }

//     public List<LogDTO> getAllLogssAsc(List<String> serviceNameList) {
//         return logQueryRepo.findAllOrderByCreatedTimeAsc(serviceNameList);
//     }

//     public List<LogDTO> getErrorLogsByServiceNamesOrderBySeverityAndCreatedTimeDesc(List<String> serviceNameList) {
//         MongoDatabase database = mongoClient.getDatabase("OtelLog");
//     MongoCollection<LogDTO> logDTOCollection = database.getCollection("LogDTO", LogDTO.class);

//     Bson matchStage = Aggregates.match(Filters.in("serviceName", serviceNameList));

//     Bson addSortFieldStage = Aggregates.addFields(new Field<>("customSortField", new Document("$cond",
//             Arrays.asList(
//                     new Document("$in", Arrays.asList("$severityText", Arrays.asList("ERROR", "SEVERE"))),
//                     0,
//                     1
//             )
//     )));

//     Bson sortStage = Aggregates.sort(Sorts.orderBy(
//             Sorts.ascending("customSortField"),
//             Sorts.descending("createdTime")
//     ));

//     Bson projectStage = Aggregates.project(Projections.exclude("customSortField"));

//     List<LogDTO> result = logDTOCollection.aggregate(Arrays.asList(matchStage, addSortFieldStage, sortStage, projectStage))
//             .into(new ArrayList<>());

//     return result;
       
//     }


    // public List<LogDTO>  searchFunction(String keyword ){
    //     return logQueryRepo.searchByStringValue(keyword);
    // }


 public List<LogDTO>  searchFunction(String keyword,int page, int pageSize, LocalDate fromDate, LocalDate toDate, Integer minutesAgo ){
        return logQueryRepo.searchFunction(keyword, page, pageSize, fromDate, toDate, minutesAgo);
    }


  

    // public List<LogDTO> filterServiceName(LogQuery query, int page, int pageSize) {
    //     PanacheQuery<LogDTO> panacheQuery = LogDTO.find(
    //             "serviceName in :serviceNames and severityText in :severityTexts",
    //             Parameters
    //                     .with("serviceNames", query.getServiceName())
    //                     .and("severityTexts", query.getSeverityText())
    //     );
    
    //     return panacheQuery.page(Page.of(page, pageSize)).list();
    // }

    // public List<LogDTO>     filterServiceName(LogQuery query, int page, int pageSize, LocalDate fromDate, LocalDate toDate, Integer minutesAgo,String sortOrder) {
    
    // return logQueryRepo.filterServiceLogs(query, page, pageSize, fromDate, toDate, minutesAgo);
    // }
        //     Instant fromInst
    //     Instant toInstant;
    
    //     if (fromDate != null && toDate != null) {
    //         Instant startOfFrom = fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
    //         Instant startOfTo = toDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
    
    //         fromInstant = startOfFrom.isBefore(startOfTo) ? startOfFrom : startOfTo;
    //         toInstant = startOfFrom.isBefore(startOfTo) ? startOfTo : startOfFrom;
    
    //         toInstant = toInstant.plus(1, ChronoUnit.DAYS);
    //     } else if (minutesAgo > 0) {
    //         Instant currentInstant = Instant.now();
    //         Instant minutesAgoInstant = currentInstant.minus(minutesAgo, ChronoUnit.MINUTES);
    
    //         // Calculate the start of the current day
    //         Instant startOfCurrentDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();
    
    //         if (minutesAgoInstant.isBefore(startOfCurrentDay)) {
    //             fromInstant = startOfCurrentDay;
    //         } else {
    //             fromInstant = minutesAgoInstant;
    //         }
    
    //         toInstant = currentInstant;
    //     } else {
    //         throw new IllegalArgumentException("Either date range or minutesAgo must be provided");
    //     }
    
    //     PanacheQuery<LogDTO> panacheQuery = LogDTO.find(
    //         "serviceName in :serviceNames and severityText in :severityTexts " +
    //         "and createdTime >= :fromDate and createdTime <= :toDate",
    //         Parameters
    //             .with("serviceNames", query.getServiceName())
    //             .and("severityTexts", query.getSeverityText())
    //             .and("fromDate", fromInstant)
    //             .and("toDate", toInstant)
    //     );
    
    //     return panacheQuery.page(Page.of(page, pageSize)).list();
    // }

//     //filter log desec
//     public List<LogDTO> getFilterLogsByCreatedTimeDesc(List<LogDTO> logs) {
//     // System.out.println("------getFilterLogsByCreatedTimeDesc---------"+logs.size());
//     return logs.stream()
// .sorted(Comparator.comparing(LogDTO::getCreatedTime, Comparator.reverseOrder()))
// .collect(Collectors.toList());

// }

// //filter log asc
// public List<LogDTO> getFilterLogssAsc(List<LogDTO> logs) {
//    return logs.stream().sorted(Comparator.comparing(LogDTO::getCreatedTime)).collect(Collectors.toList());
// }


// //filter log error first
// public List<LogDTO> getFilterErrorLogs(List<LogDTO> logs) {
//         return logs.stream()
//             .sorted(Comparator
//                 .comparing((LogDTO log) -> {
//                     String severityText = log.getSeverityText();
//                     return ("ERROR".equals(severityText) || "SEVERE".equals(severityText)) ? 0 : 1;
//                 })
//                 .thenComparing(LogDTO::getCreatedTime, Comparator.nullsLast(Comparator.reverseOrder()))
//             )
//             .collect(Collectors.toList());
//     }
// }
// public List<LogMetrics> getLogMetricCount(int minutesAgo, LocalDate fromDate, LocalDate toDate, List<String> serviceNameList) {
    
//     List<LogDTO> logList = logQueryRepo.listAll();
//     Map<String, LogMetrics> metricsMap = new HashMap<>();

//     Instant fromInstant;
//     Instant toInstant;

//     if (fromDate != null && toDate != null) {
//         Instant startOfFrom = fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
//         Instant startOfTo = toDate.atStartOfDay(ZoneId.systemDefault()).toInstant();

//         fromInstant = startOfFrom.isBefore(startOfTo) ? startOfFrom : startOfTo;
//         toInstant = startOfFrom.isBefore(startOfTo) ? startOfTo : startOfFrom;

//         toInstant = toInstant.plus(1, ChronoUnit.DAYS);
//     } else if (minutesAgo > 0) {
//         Instant currentInstant = Instant.now();
//         Instant minutesAgoInstant = currentInstant.minus(minutesAgo, ChronoUnit.MINUTES);

//         Instant startOfCurrentDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();
//         fromInstant = minutesAgoInstant.isBefore(startOfCurrentDay) ? startOfCurrentDay : minutesAgoInstant;
//         toInstant = currentInstant;
//     } else {
//         throw new IllegalArgumentException("Either date range or minutesAgo must be provided");
//     }

//     for (LogDTO logDTO : logList) {
//         Date logCreateTime = logDTO.getCreatedTime();
//         if (logCreateTime != null) {
//             Instant logInstant = logCreateTime.toInstant();

//             if (logInstant.isAfter(fromInstant) && logInstant.isBefore(toInstant)
//                     && serviceNameList.contains(logDTO.getServiceName())) {
//                 String serviceName = logDTO.getServiceName();
//                 LogMetrics metrics = metricsMap.computeIfAbsent(serviceName, k -> new LogMetrics());

//                 // Increment counts based on severity
//                 calculateCallCounts(logDTO, metrics);
//             }
//         }
//     }

//     // Print metrics for debugging
//     for (Map.Entry<String, LogMetrics> entry : metricsMap.entrySet()) {
//         LogMetrics metrics = entry.getValue();
//         System.out.println(String.format("Service: %s, Error Count: %d, Warn Count: %d, Debug Count: %d",
//                 entry.getKey(), metrics.getErrorCallCount(), metrics.getWarnCallCount(), metrics.getDebugCallCount()));
//     }

//     return new ArrayList<>(metricsMap.values());
// }
// private void calculateCallCounts(LogDTO logDTO, LogMetrics metrics) {
//     if (logDTO != null && logDTO.getScopeLogs() != null) {
//         for (ScopeLogs scopeLogs : logDTO.getScopeLogs()) {
//             if (scopeLogs != null && scopeLogs.getLogRecords() != null) {
//                 for (LogRecord logRecord : scopeLogs.getLogRecords()) {
//                     if (logRecord != null) {
//                         String severityText = logRecord.getSeverityText();
//                         if (severityText != null) {
//                             switch (severityText.toUpperCase()) {
//                                 case "ERROR":
//                                     metrics.setErrorCallCount(metrics.getErrorCallCount() + 1);
//                                     break;
//                                 case "WARN":
//                                     metrics.setWarnCallCount(metrics.getWarnCallCount() + 1);
//                                     break;
//                                 case "DEBUG":
//                                     metrics.setDebugCallCount(metrics.getDebugCallCount() + 1);
//                                     break;
//                                 // Add other cases as needed
//                                 default:
//                                     // Handle other severityText values if needed
//                                     break;
//                             }
//                         } else {
//                             System.out.println("SeverityText is null");
//                             // Handle the case when severityText is null
//                         }
//                     } else {
//                         System.out.println("LogRecord is null");
//                         // Handle the case when logRecord is null
//                     }
//                 }
//             } else {
//                 System.out.println("ScopeLogs or LogRecords is null");
//                 // Handle the case when scopeLogs or LogRecords is null
//             }
//         }
//     } else {
//         System.out.println("LogDTO is null");
//         // Handle the case when logDTO is null
//     }
// }





public List<LogDTO> filterServiceName(LogQuery query, LocalDate fromDate, LocalDate toDate, Integer minutesAgo, String sortOrder) {
    Instant fromInstant;
    Instant toInstant;

    if (fromDate != null && toDate != null) {
        Instant startOfFrom = fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant startOfTo = toDate.atStartOfDay(ZoneId.systemDefault()).toInstant();

        fromInstant = startOfFrom.isBefore(startOfTo) ? startOfFrom : startOfTo;
        toInstant = startOfFrom.isBefore(startOfTo) ? startOfTo : startOfFrom;

        toInstant = toInstant.plus(1, ChronoUnit.DAYS);
    } else if (minutesAgo > 0) {
        Instant currentInstant = Instant.now();
        Instant minutesAgoInstant = currentInstant.minus(minutesAgo, ChronoUnit.MINUTES);

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

    List<Bson> filters = new ArrayList<>();

    if (query.getServiceName() != null) {
        filters.add(Filters.in("serviceName", query.getServiceName()));
    } else if (query.getSeverityText() != null) {
        filters.add(Filters.in("severityText", query.getSeverityText()));
    }

    filters.add(Filters.gte("createdTime", fromInstant));
    filters.add(Filters.lte("createdTime", toInstant));

    Bson match = Aggregates.match(Filters.and(filters));

    List<LogDTO> result = LogDTO.mongoCollection()
            .withDocumentClass(LogDTO.class)
            .aggregate(Arrays.asList(match), LogDTO.class)
            .into(new ArrayList<>());

    return result;
}

// public List<LogDTO> filterServiceName(LogQuery query, LocalDate fromDate, LocalDate toDate, Integer minutesAgo, String sortOrder) {
//     Instant fromInstant;
//     Instant toInstant;

//     if (fromDate != null && toDate != null) {
//         Instant startOfFrom = fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
//         Instant startOfTo = toDate.atStartOfDay(ZoneId.systemDefault()).toInstant();

//         fromInstant = startOfFrom.isBefore(startOfTo) ? startOfFrom : startOfTo;
//         toInstant = startOfFrom.isBefore(startOfTo) ? startOfTo : startOfFrom;

//         toInstant = toInstant.plus(1, ChronoUnit.DAYS);
//     } else if (minutesAgo > 0) {
//         Instant currentInstant = Instant.now();
//         Instant minutesAgoInstant = currentInstant.minus(minutesAgo, ChronoUnit.MINUTES);

//         Instant startOfCurrentDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();

//         if (minutesAgoInstant.isBefore(startOfCurrentDay)) {
//             fromInstant = startOfCurrentDay;
//         } else {
//             fromInstant = minutesAgoInstant;
//         }

//         toInstant = currentInstant;
//     } else {
//         throw new IllegalArgumentException("Either date range or minutesAgo must be provided");
//     }

//     List<Bson> filters = new ArrayList<>();

//     if (query.getServiceName() != null) {
//         filters.add(Filters.in("serviceName", query.getServiceName()));
//     } else if (query.getSeverityText() != null) {
//         filters.add(Filters.in("severityText", query.getSeverityText()));
//     }

//     filters.add(Filters.gte("createdTime", fromInstant));
//     filters.add(Filters.lte("createdTime", toInstant));

//     Bson match = Aggregates.match(Filters.and(filters));

//     // Exclude createdTime field from the result
//     Bson projection = Projections.exclude("createdTime");

//     List<LogDTO> result = LogDTO.mongoCollection()
//             .withDocumentClass(LogDTO.class)
//             .aggregate(Arrays.asList(match, projection), LogDTO.class)
//             .into(new ArrayList<>());

//     // Convert UTC timestamps to formatted strings
//     SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

//     for (LogDTO logDTO : result) {
//         Date logDate = logDTO.getCreatedTime();
//         String formattedDateTime = dateFormat.format(logDate);
//         logDTO.setCreatedTime(formattedDateTime);
//     }

//     return result;
// }


public List<LogMetrics> getLogMetricCount(int minutesAgo, LocalDate from, LocalDate to, List<String> serviceNameList) {
    System.out.println("from: " + from);
    System.out.println("to: " + to);
    System.out.println("minutesAgo: " + minutesAgo);

    List<LogDTO> logList = logQueryRepo.listAll();
    Map<String, LogMetrics> metricsMap = new HashMap<>();

    Instant fromInstant;
    Instant toInstant;

    if (from != null && to != null) {
        fromInstant = from.atStartOfDay(ZoneId.systemDefault()).toInstant();
        toInstant = to.atStartOfDay(ZoneId.systemDefault()).toInstant().plus(1, ChronoUnit.DAYS);
    } else if (minutesAgo > 0) {
        Instant currentInstant = Instant.now();
        Instant minutesAgoInstant = currentInstant.minus(minutesAgo, ChronoUnit.MINUTES);
        Instant startOfCurrentDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();

        fromInstant = minutesAgoInstant.isBefore(startOfCurrentDay) ? startOfCurrentDay : minutesAgoInstant;
        toInstant = currentInstant;
    } else {
        throw new IllegalArgumentException("Either date range or minutesAgo must be provided");
    }

    for (LogDTO logDTO : logList) {
        Date logCreateTime = logDTO.getCreatedTime();
        if (logCreateTime != null) {
            Instant logInstant = logCreateTime.toInstant();

            if (logInstant.isAfter(fromInstant) && logInstant.isBefore(toInstant)
                    && serviceNameList.contains(logDTO.getServiceName())) {
                String serviceName = logDTO.getServiceName();
                LogMetrics metrics = metricsMap.computeIfAbsent(serviceName, k -> new LogMetrics(serviceName, 0L, 0L, 0L));

                calculateCallCounts(logDTO, metrics);
            }
        }
    }

    return new ArrayList<>(metricsMap.values());
}

private void calculateCallCounts(LogDTO logDTO, LogMetrics metrics) {
    for (ScopeLogs scopeLogs : logDTO.getScopeLogs()) {
        for (LogRecord logRecord : scopeLogs.getLogRecords()) {
            String severityText = logRecord.getSeverityText();
            switch (severityText) {
                case "ERROR":
                case "SEVERE":
                    metrics.setErrorCallCount(metrics.getErrorCallCount() + 1);
                    break;
                case "WARN":
                    metrics.setWarnCallCount(metrics.getWarnCallCount() + 1);
                    break;
                case "DEBUG":
                    metrics.setDebugCallCount(metrics.getDebugCallCount() + 1);
                    break;
                // Add more cases if needed
            }
        }
    }
}

  //sort orer decending 
  public List<LogDTO> getAllLogssOrderByCreatedTimeDesc(List<String> serviceNameList) {
    return logQueryRepo.findOrderByCreatedTimeDesc(serviceNameList);
  }


//sort order ascending
public List<LogDTO> getAllLogssAsc(List<String> serviceNameList) {
    return logQueryRepo.findOrderByCreatedTimeAsc(serviceNameList);
}

//sort order error data decending
public List<LogDTO> getErrorLogsByServiceNamesOrderBySeverityAndCreatedTimeDesc(List<String> serviceNameList) {
    MongoDatabase database = mongoClient.getDatabase("OtelLog");
    MongoCollection<LogDTO> logDTOCollection = database.getCollection("LogDTO", LogDTO.class);

    Bson matchStage = Aggregates.match(Filters.in("serviceName", serviceNameList));

    Bson addSortFieldStage = Aggregates.addFields(new Field<>("customSortField", new Document("$cond",
            Arrays.asList(
                    new Document("$in", Arrays.asList("$severityText", Arrays.asList("ERROR", "SEVERE"))),
                    0,
                    1
            )
    )));

    Bson sortStage = Aggregates.sort(Sorts.orderBy(
            Sorts.ascending("customSortField"),
            Sorts.descending("createdTime")
    ));

    Bson projectStage = Aggregates.project(Projections.exclude("customSortField"));

    List<LogDTO> result = logDTOCollection.aggregate(Arrays.asList(matchStage, addSortFieldStage, sortStage, projectStage))
            .into(new ArrayList<>());

    return result;
}


}

   

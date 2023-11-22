package com.graphql.handler.query;



import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.graphql.entity.otellog.ScopeLogs;
import com.graphql.entity.otellog.scopeLogs.LogRecord;
import com.graphql.entity.otellog.scopeLogs.Scope;
import com.graphql.entity.otellog.scopeLogs.logRecord.Body;
import com.graphql.entity.queryentity.log.LogDTO;
import com.graphql.entity.queryentity.log.LogMetrics;
import com.graphql.entity.queryentity.log.LogQuery;
import com.graphql.entity.queryentity.trace.TraceDTO;
import com.graphql.entity.queryentity.trace.TraceQuery;
import com.graphql.repo.query.LogQueryRepo;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;

import io.quarkus.mongodb.panache.PanacheMongoEntityBase;
import io.quarkus.mongodb.panache.PanacheQuery;
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
   
    public List<LogDTO> getlogByServiceName(String serviceName) {
        return logQueryRepo.findByServiceName(serviceName);
    }








//logmetrics using to data and time based

// public List<LogMetrics> getLogMetricCount(List<String> serviceNameList, LocalDate from, LocalDate to, int minutesAgo) {
//     System.out.println("from: " + from);
//     System.out.println("to: " + to);
//     System.out.println("minutesAgo: " + minutesAgo);

//     List<LogDTO> logList = logQueryRepo.listAll();
//     Map<String, LogMetrics> metricsMap = new HashMap<>();

//     Instant fromInstant = null;
//     Instant toInstant = null;

//     if (from != null && to != null) {
//         Instant startOfFrom = from.atStartOfDay(ZoneId.systemDefault()).toInstant();
//         Instant startOfTo = to.atStartOfDay(ZoneId.systemDefault()).toInstant();

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

//     for (LogDTO logDTO : logList) {
//         Date logCreateTime = logDTO.getCreatedTime();
//         if (logCreateTime != null) {
//             Instant logInstant = logCreateTime.toInstant();

//             if (logInstant.isAfter(fromInstant) && logInstant.isBefore(toInstant)
//                     && serviceNameList.contains(logDTO.getServiceName())) {
//                 String serviceName = logDTO.getServiceName();
//                 LogMetrics metrics = metricsMap.get(serviceName);

//                 if (metrics == null) {
//                     metrics = new LogMetrics();
//                     metrics.setServiceName(serviceName);
//                     metrics.setErrorCallCount(0L);
//                     metrics.setWarnCallCount(0L);
//                     metrics.setDebugCallCount(0L);
//                     metricsMap.put(serviceName, metrics);
//                 }

//                 calculateCallCounts(logDTO, metrics);
//             }
//         }
//     }

//     return new ArrayList<>(metricsMap.values());
// }



// private void calculateCallCounts(LogDTO logDTO, LogMetrics metrics) {
//     for (ScopeLogs scopeLogs : logDTO.getScopeLogs()) {
//         for (LogRecord logRecord : scopeLogs.getLogRecords()) {
//             String severityText = logRecord.getSeverityText(); 
//             if ("ERROR".equals(severityText) || "SEVERE".equals(severityText)) {
//                 metrics.setErrorCallCount(metrics.getErrorCallCount() + 1);
//             } else if ("WARN".equals(severityText)) {
//                 metrics.setWarnCallCount(metrics.getWarnCallCount() + 1);
//             } else if ("DEBUG".equals(severityText)) {
//                 metrics.setDebugCallCount(metrics.getDebugCallCount() + 1);
//             }
//         }
//     }
// }


// Search query date wised fetch the data

// public List<LogDTO> searchLogsPaged(LogQuery logQuery, LocalDate from, LocalDate to) {
//     List<String> serviceNames = logQuery.getServiceName();
//     List<String> severityTexts = logQuery.getSeverityText();
    
//     List<LogDTO> logList = logQueryRepo.listAll();
    
//     Instant fromInstant;
//     Instant toInstant;

//     if (from != null && to != null) {
//         Instant startOfFrom = from.atStartOfDay(ZoneId.systemDefault()).toInstant();
//         Instant startOfTo = to.atStartOfDay(ZoneId.systemDefault()).toInstant();

//         fromInstant = startOfFrom.isBefore(startOfTo) ? startOfFrom : startOfTo;
//         toInstant = startOfFrom.isBefore(startOfTo) ? startOfTo : startOfFrom;

//         // Adjusted value to be used within lambda
//         final Instant adjustedToInstant = toInstant.plus(1, ChronoUnit.DAYS);

//         // Use adjustedToInstant within the lambda expression
//         List<LogDTO> filteredAndSortedLogs = logList.stream()
//                 .filter(logDTO -> (serviceNames == null || serviceNames.isEmpty() || serviceNames.contains(logDTO.getServiceName())) &&
//                         (severityTexts == null || severityTexts.isEmpty() || severityTexts.contains(logDTO.getSeverityText())))
//                 .filter(logDTO -> isWithinDateRange(logDTO.getCreatedTime().toInstant(), fromInstant, adjustedToInstant))
//                 .collect(Collectors.toList());

//         filteredAndSortedLogs.sort(Comparator.comparing(LogDTO::getCreatedTime).reversed());

//         return filteredAndSortedLogs;
//     } else {
//         throw new IllegalArgumentException("Both from and to dates must be provided");
//     }
// }

// private boolean isWithinDateRange(Instant logTimestamp, Instant from, Instant to) {
//     LocalDateTime logDateTime = logTimestamp.atZone(ZoneId.systemDefault()).toLocalDateTime();
    
//     return (logDateTime.isEqual(from.atZone(ZoneId.systemDefault()).toLocalDateTime()) || logDateTime.isAfter(from.atZone(ZoneId.systemDefault()).toLocalDateTime())) &&
//             (logDateTime.isEqual(to.atZone(ZoneId.systemDefault()).toLocalDateTime()) || logDateTime.isBefore(to.atZone(ZoneId.systemDefault()).toLocalDateTime()));
// }




// anyone date give (from or to)
// public List<LogDTO> searchLogsPaged(LogQuery logQuery, LocalDate from, LocalDate to) {
//     List<String> serviceNames = logQuery.getServiceName();
//     List<String> severityTexts = logQuery.getSeverityText();
    
//     List<LogDTO> logList = logQueryRepo.listAll();
    
//     Instant fromInstant;
//     Instant toInstant;

//     if ((from != null && to != null) || (from == null && to == null)) {
//         throw new IllegalArgumentException("Either both from and to dates or none must be provided");
//     } else if (from != null) {
//         fromInstant = from.atStartOfDay(ZoneId.systemDefault()).toInstant();
//         toInstant = fromInstant.plus(1, ChronoUnit.DAYS);
//     } else {
//         toInstant = to.atStartOfDay(ZoneId.systemDefault()).toInstant();
//         fromInstant = toInstant.minus(1, ChronoUnit.DAYS);
//     }

//     // Use adjustedToInstant within the lambda expression
//     List<LogDTO> filteredAndSortedLogs = logList.stream()
//             .filter(logDTO -> (serviceNames == null || serviceNames.isEmpty() || serviceNames.contains(logDTO.getServiceName())) &&
//                     (severityTexts == null || severityTexts.isEmpty() || severityTexts.contains(logDTO.getSeverityText())))
//             .filter(logDTO -> isWithinDateRange(logDTO.getCreatedTime().toInstant(), fromInstant, toInstant))
//             .collect(Collectors.toList());

//     filteredAndSortedLogs.sort(Comparator.comparing(LogDTO::getCreatedTime).reversed());

//     return filteredAndSortedLogs;
// }

// private boolean isWithinDateRange(Instant logTimestamp, Instant from, Instant to) {
//     LocalDateTime logDateTime = logTimestamp.atZone(ZoneId.systemDefault()).toLocalDateTime();
    
//     return (logDateTime.isEqual(from.atZone(ZoneId.systemDefault()).toLocalDateTime()) || logDateTime.isAfter(from.atZone(ZoneId.systemDefault()).toLocalDateTime())) &&
//             (logDateTime.isEqual(to.atZone(ZoneId.systemDefault()).toLocalDateTime()) || logDateTime.isBefore(to.atZone(ZoneId.systemDefault()).toLocalDateTime()));
// }




// given one date to minutesago using
  
// public List<LogDTO> searchLogsPaged(LogQuery logQuery, LocalDate from, int minutesAgo) {
//     List<String> serviceNames = logQuery.getServiceName();
//     List<String> severityTexts = logQuery.getSeverityText();

//     List<LogDTO> logList = logQueryRepo.listAll();

//     Instant toInstant = Instant.now();
//     Instant fromInstant = toInstant.minus(minutesAgo, ChronoUnit.MINUTES);
  


//     // Use adjustedToInstant within the lambda expression
//     List<LogDTO> filteredAndSortedLogs = logList.stream()
//             .filter(logDTO -> (serviceNames == null || serviceNames.isEmpty() || serviceNames.contains(logDTO.getServiceName())) &&
//                     (severityTexts == null || severityTexts.isEmpty() || severityTexts.contains(logDTO.getSeverityText())))
//             .filter(logDTO -> isWithinDateRange(logDTO.getCreatedTime().toInstant(), fromInstant, toInstant))
//             .collect(Collectors.toList());

//     filteredAndSortedLogs.sort(Comparator.comparing(LogDTO::getCreatedTime).reversed());

//     return filteredAndSortedLogs;
// }

// private boolean isWithinDateRange(Instant logTimestamp, Instant from, Instant to) {
//     LocalDateTime logDateTime = logTimestamp.atZone(ZoneId.systemDefault()).toLocalDateTime();

//     return (logDateTime.isEqual(from.atZone(ZoneId.systemDefault()).toLocalDateTime()) || logDateTime.isAfter(from.atZone(ZoneId.systemDefault()).toLocalDateTime())) &&
//             (logDateTime.isEqual(to.atZone(ZoneId.systemDefault()).toLocalDateTime()) || logDateTime.isBefore(to.atZone(ZoneId.systemDefault()).toLocalDateTime()));
// }


//From date and To date and minutesago is anyone is optional
public List<LogDTO> searchLogsPaged(LogQuery logQuery, LocalDate fromDate, LocalDate toDate, Integer minutesAgo) {
    List<String> serviceNames = logQuery.getServiceName();
    List<String> severityTexts = logQuery.getSeverityText();

    List<LogDTO> logList = logQueryRepo.listAll();

    Instant toInstant = toDate != null ? toDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant() : Instant.now();
    Instant fromInstant;

    if (fromDate != null) {
        fromInstant = fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
    } else if (minutesAgo != null) {
        fromInstant = toInstant.minus(minutesAgo, ChronoUnit.MINUTES);
    } else {
        // Set a default value if none of from date and minutesAgo are provided
        fromInstant = Instant.EPOCH;
    }

    List<LogDTO> filteredAndSortedLogs = logList.stream()
            .filter(logDTO -> (serviceNames == null || serviceNames.isEmpty() || serviceNames.contains(logDTO.getServiceName())) &&
                    (severityTexts == null || severityTexts.isEmpty() || severityTexts.contains(logDTO.getSeverityText())))
            .filter(logDTO -> isWithinDateRange(logDTO.getCreatedTime().toInstant(), fromInstant, toInstant))
            .collect(Collectors.toList());

    filteredAndSortedLogs.sort(Comparator.comparing(LogDTO::getCreatedTime).reversed());

    return filteredAndSortedLogs;
}

private boolean isWithinDateRange(Instant logTimestamp, Instant from, Instant to) {
    return !logTimestamp.isBefore(from) && !logTimestamp.isAfter(to);
}




















}





























   


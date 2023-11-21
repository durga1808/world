package com.graphql.handler.query;



import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.graphql.entity.otellog.ScopeLogs;
import com.graphql.entity.otellog.scopeLogs.LogRecord;
import com.graphql.entity.otellog.scopeLogs.Scope;
import com.graphql.entity.otellog.scopeLogs.logRecord.Body;
import com.graphql.entity.queryentity.log.LogDTO;
import com.graphql.entity.queryentity.log.LogMetrics;
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
        List<LogDTO> logs =logQueryRepo.listAll();
        return logs;
    }
   
    public List<LogDTO> getlogByServiceName(String serviceName) {
        return logQueryRepo.findByServiceName(serviceName);
    }

//     public List<LogDTO> searchLogs(LocalDate from, LocalDate to, int minutesAgo) {
        
//     List<LogDTO> results = new ArrayList<>();

//     try {
//         MongoCollection<Document> collection = mongoClient
//                 .getDatabase("OtelLog")
//                 .getCollection("LogDTO");

//         Document query = new Document();

//         if (from != null && to != null) {
            
//             Instant fromInstant = from.atStartOfDay(ZoneId.systemDefault()).toInstant();
//             Instant toInstant = to.atStartOfDay(ZoneId.systemDefault()).toInstant().plus(1, ChronoUnit.DAYS);

//             query.append("createdTime", new Document("$gte", Date.from(fromInstant)).append("$lt", Date.from(toInstant)));
//         } else if (minutesAgo > 0) {
//             Instant currentInstant = Instant.now();
//             Instant fromInstant = currentInstant.minus(minutesAgo, ChronoUnit.MINUTES);
//             Instant toInstant = currentInstant.minus(1, ChronoUnit.MINUTES);

//             query.append("createdTime", new Document("$gte", Date.from(fromInstant)).append("$lt", Date.from(toInstant)));
//         }

//         MongoCursor<Document> cursor = collection.find(query).iterator();

//         while (cursor.hasNext()) {
//             Document document = cursor.next();
//             LogDTO logResult = mapDocumentToLogDTO(document);
//             results.add(logResult);
//         }
//     } catch (Exception e) {
//         // Handle exceptions
//     }

//     return results;
// }
// private LogDTO mapDocumentToLogDTO(Document document) {
//     LogDTO logDTO = new LogDTO();

//     // Set other fields as needed based on your document structure
//     logDTO.setServiceName(document.getString("serviceName"));
//     logDTO.setTraceId(document.getString("traceId"));
//     logDTO.setSpanId(document.getString("spanId"));
//     logDTO.setCreatedTime(document.getDate("createdTime"));
//     logDTO.setSeverityText(document.getString("severityText"));

//     // Set scope and logRecords based on your document structure
//     Scope scope = new Scope();
//     List<LogRecord> logRecords = new ArrayList<>();

//     List<Document> scopeLogsList = document.getList("scopeLogs", Document.class);
//     if (scopeLogsList != null) {
//         for (Document scopeLog : scopeLogsList) {
//             scope.setName(scopeLog.getString("scope.name"));

//             List<Document> logRecordsDocuments = scopeLog.getList("logRecords", Document.class);
//             if (logRecordsDocuments != null) {
//                 for (Document logRecordDocument : logRecordsDocuments) {
//                     LogRecord logRecord = new LogRecord();
//                     logRecord.setTimeUnixNano(logRecordDocument.getString("timeUnixNano"));
//                     logRecord.setObservedTimeUnixNano(logRecordDocument.getString("observedTimeUnixNano"));
//                     logRecord.setSeverityNumber(logRecordDocument.getInteger("severityNumber", 0));
//                     logRecord.setSeverityText(logRecordDocument.getString("severityText"));
//                     logRecord.setFlags(logRecordDocument.getInteger("flags", 0));
//                     logRecord.setTraceId(logRecordDocument.getString("traceId"));
//                     logRecord.setSpanId(logRecordDocument.getString("spanId"));

//                     Body body = new Body();
//                     body.setStringValue(logRecordDocument.getEmbedded(List.of("body", "stringValue"), String.class));
//                     logRecord.setBody(body);

//                     logRecords.add(logRecord);
//                 }
//             }
//         }
//     }

//     ScopeLogs scopeLogs = new ScopeLogs();
//     scopeLogs.setScope(scope);
//     scopeLogs.setLogRecords(logRecords);
//     List<ScopeLogs> scopeLogsArray = new ArrayList<>();
//     scopeLogsArray.add(scopeLogs);
//     logDTO.setScopeLogs(scopeLogsArray);

//     return logDTO;
// }

// public List<LogDTO> getLogDTOList(String serviceName, LocalDate from, LocalDate to, int minutesAgo) {
//     System.out.println("from: " + from);
//     System.out.println("to: " + to);
//     System.out.println("minutesAgo: " + minutesAgo);

//     List<LogDTO> logList = logQueryRepo.listAll();
//     Map<String, LogDTO> logDTOMap = new HashMap<>();

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
//                     && serviceName.contains(logDTO.getServiceName())) {
//                 // String serviceName = logDTO.getServiceName();
//                 LogDTO existingLogDTO = logDTOMap.get(serviceName);

//                 if (existingLogDTO == null) {
//                     existingLogDTO = new LogDTO();
//                     // Copy relevant fields from logDTO to existingLogDTO
//                     existingLogDTO.setServiceName(serviceName);
//                     // Copy other fields as needed
//                     logDTOMap.put(serviceName, existingLogDTO);
//                 }

//                 // Update existingLogDTO with logDTO values
//                 // (e.g., call some method like calculateCallCounts, if applicable)
//                 // calculateCallCounts(logDTO, existingLogDTO);
//             }
//         }
//     }

//     return new ArrayList<>(logDTOMap.values());
// }

// public List<LogDTO> getLogDTOList( LocalDate from, LocalDate to, int minutesAgo) {
  
//         System.out.println("from: " + from);
//     System.out.println("to: " + to);
//     System.out.println("minutesAgo: " + minutesAgo);

//     List<LogDTO> logList = logQueryRepo.listAll();
//     Map<String, LogDTO> logDTOMap = new HashMap<>();

//     Instant fromInstant = null;
//     Instant toInstant = null;

//     ZoneId istZoneId = ZoneId.of("Asia/Kolkata"); // IST time zone

//     if (from != null && to != null) {
//         Instant startOfFrom = from.atStartOfDay(istZoneId).toInstant();
//         Instant startOfTo = to.atStartOfDay(istZoneId).toInstant();

//         fromInstant = startOfFrom.isBefore(startOfTo) ? startOfFrom : startOfTo;
//         toInstant = startOfFrom.isBefore(startOfTo) ? startOfTo : startOfFrom;

//         toInstant = toInstant.plus(1, ChronoUnit.DAYS);
//     } else if (minutesAgo > 0) {
//         Instant currentInstant = Instant.now();
//         Instant minutesAgoInstant = currentInstant.minus(minutesAgo, ChronoUnit.MINUTES);

//         Instant startOfCurrentDay = LocalDate.now(istZoneId).atStartOfDay(istZoneId).toInstant();

//         if (minutesAgoInstant.isBefore(startOfCurrentDay)) {
//             fromInstant = startOfCurrentDay;
//         } else {
//             fromInstant = minutesAgoInstant;
//         }

    
    
   
//     for (LogDTO logDTO : logList) {
//         Date logCreateTime = logDTO.getCreatedTime();
//         if (logCreateTime != null) {
//             Instant logInstant = logCreateTime.toInstant();

//             if (logInstant.isAfter(fromInstant) && logInstant.isBefore(toInstant)
//                     && serviceName.contains(logDTO.getServiceName())) {
//                 // String serviceName = logDTO.getServiceName();
//                 LogDTO existingLogDTO = logDTOMap.get(serviceName);

//                 if (existingLogDTO == null) {
//                     existingLogDTO = new LogDTO();
//                     // Copy relevant fields from logDTO to existingLogDTO
//                     existingLogDTO.setServiceName(serviceName);
//                     // Copy other fields as needed
//                     logDTOMap.put(serviceName, existingLogDTO);
//                 }

//                 // Update existingLogDTO with logDTO values
//                 // (e.g., call some method like calculateCallCounts, if applicable)
//                 // calculateCallCounts(logDTO, existingLogDTO);
//             }
//         }
//     }

//     // ... (existing code)

//     return new ArrayList<>(logDTOMap.values());
// }
//     return logList;

// }





// public List<LogDTO> getLogDTOList(LocalDate from, LocalDate to, int minutesAgo) {
//     System.out.println("from: " + from);
//     System.out.println("to: " + to);
//     System.out.println("minutesAgo: " + minutesAgo);

//     List<LogDTO> logList = logQueryRepo.listAll();
//     Map<String, LogDTO> logDTOMap = new HashMap<>();

//     Instant fromInstant = null;
//     Instant toInstant = null;

//     ZoneId istZoneId = ZoneId.of("Asia/Kolkata"); // IST time zone

//     if (from != null && to != null) {
//         Instant startOfFrom = from.atStartOfDay(istZoneId).toInstant();
//         Instant startOfTo = to.atStartOfDay(istZoneId).toInstant();

//         fromInstant = startOfFrom.isBefore(startOfTo) ? startOfFrom : startOfTo;
//         toInstant = startOfFrom.isBefore(startOfTo) ? startOfTo : startOfFrom;

//         toInstant = toInstant.plus(1, ChronoUnit.DAYS);
//     } else if (minutesAgo > 0) {
//         Instant currentInstant = Instant.now();
//         Instant minutesAgoInstant = currentInstant.minus(minutesAgo, ChronoUnit.MINUTES);

//         Instant startOfCurrentDay = LocalDate.now(istZoneId).atStartOfDay(istZoneId).toInstant();

//         if (minutesAgoInstant.isBefore(startOfCurrentDay)) {
//             fromInstant = startOfCurrentDay;
//         } else {
//             fromInstant = minutesAgoInstant;
//         }

//         for (LogDTO logDTO : logList) {
//             Date logCreateTime = logDTO.getCreatedTime();
//             if (logCreateTime != null) {
//                 Instant logInstant = logCreateTime.toInstant();

//                 if (logInstant.isAfter(fromInstant) && logInstant.isBefore(toInstant)) {
//                     LogDTO existingLogDTO = logDTOMap.computeIfAbsent(logDTO.getServiceName(), k -> new LogDTO());
                    
//                     // Copy relevant fields from logDTO to existingLogDTO
//                     existingLogDTO.setServiceName(logDTO.getServiceName());
//                     // Copy other fields as needed

//                     // Update existingLogDTO with logDTO values
//                     // (e.g., call some method like calculateCallCounts, if applicable)
//                     // calculateCallCounts(logDTO, existingLogDTO);
//                 }
//             }
//         }
//     }

//     // ... (existing code)

//     return new ArrayList<>(logDTOMap.values());
// }



// public List<LogDTO> getLogData(LocalDate from, LocalDate to, String serviceName, int minutesAgo) {
//     Bson timeFilter;

//     // Rearrange 'from' and 'to' if 'to' is earlier than 'from'
//     if (from != null && to != null && to.isBefore(from)) {
//         LocalDate temp = from;
//         from = to;
//         to = temp;
//     }

//     if (from != null && to != null) {
//         timeFilter = createCustomDateFilter(from, to);

//         // If minutesAgo is provided, also include a filter for the last 'minutesAgo' minutes
//         if (minutesAgo > 0) {
//             LocalDateTime currentDateTime = LocalDateTime.now();
//             LocalDateTime fromDateTime = currentDateTime.minusMinutes(minutesAgo);
//             Bson minutesAgoFilter = Filters.and(
//                 Filters.gte("date", fromDateTime),
//                 Filters.lt("date", currentDateTime)
//             );
//             timeFilter = Filters.and(timeFilter, minutesAgoFilter);
//         }
//     } else if (minutesAgo > 0) {
//         // Calculate 'fromDateTime' based on 'minutesAgo'
//         LocalDateTime currentDateTime = LocalDateTime.now();
//         LocalDateTime fromDateTime = currentDateTime.minusMinutes(minutesAgo);

//         timeFilter = Filters.and(
//             Filters.gte("date", fromDateTime),
//             Filters.lt("date", currentDateTime)
//         );
//     } else {
//         // Handle the case when neither date range nor minutesAgo is provided
//         throw new IllegalArgumentException("Either date range or minutesAgo must be provided");
//     }

//     Bson serviceNameFilter = Filters.eq("serviceName", serviceName);
//     Bson finalFilter = Filters.and(timeFilter, serviceNameFilter);

//     MongoCollection<Document> collection = mongoClient
//         .getDatabase("OtelLog")
//         .getCollection("LogDTO");

//     List<LogDTO> filteredResults = new ArrayList<>();

//     try (MongoCursor<Document> cursor = collection.find(finalFilter).iterator()) {
//         while (cursor.hasNext()) {
//             Document document = cursor.next();
//             LogDTO logDTO = convertDocumentToLogDTO(document);
//             filteredResults.add(logDTO);
//         }
//     }

//     return filteredResults;
// }

// private LogDTO convertDocumentToLogDTO(Document document) {
//     LogDTO logDTO = new LogDTO();
      
//     // Assuming you have a LocalDateTime field for date in LogDTO
//     // logDTO.setDate(Date.from(document.getDate("date").toInstant()));
//     logDTO.setServiceName(document.getString("serviceName"));

//     return logDTO;
// }

// private Bson createCustomDateFilter(LocalDate from, LocalDate to) {
//     return Filters.and(
//         Filters.gte("date", from.atStartOfDay()),
//         Filters.lt("date", to.plusDays(1).atStartOfDay())
//     );


// }




public List<LogDTO> getLogDTOList(List<String> serviceNameList, LocalDate from, LocalDate to, int minutesAgo) {
    System.out.println("from: " + from);
    System.out.println("to: " + to);
    System.out.println("minutesAgo: " + minutesAgo);

    List<LogDTO> logList = logQueryRepo.listAll();
    Map<String, LogDTO> logDTOMap = new HashMap<>();

    Instant fromInstant = null;
    Instant toInstant = null;

    if (from != null && to != null) {
        Instant startOfFrom = from.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant startOfTo = to.atStartOfDay(ZoneId.systemDefault()).toInstant();

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

    for (LogDTO logDTO : logList) {
        Date logCreateTime = logDTO.getCreatedTime();
        if (logCreateTime != null) {
            Instant logInstant = logCreateTime.toInstant();

            if (logInstant.isAfter(fromInstant) && logInstant.isBefore(toInstant)
                    && serviceNameList.contains(logDTO.getServiceName())) {
                String serviceName = logDTO.getServiceName();
                LogDTO logInMap = logDTOMap.get(serviceName);

                if (logInMap == null) {
                    logDTOMap.put(serviceName, logDTO);
                } else {
                    calculateCallCounts(logDTO, logInMap);
                }
            }
        }
    }

    return new ArrayList<>(logDTOMap.values());
}

private void calculateCallCounts(LogDTO sourceLogDTO, LogDTO targetLogDTO) {
    // Adjust the logic to accumulate call counts in the targetLogDTO
    // based on the sourceLogDTO's call counts

    for (ScopeLogs scopeLogs : sourceLogDTO.getScopeLogs()) {
        for (LogRecord logRecord : scopeLogs.getLogRecords()) {
            String severityText = logRecord.getSeverityText();
            // Adjust the logic to accumulate call counts in targetLogDTO
            // based on the severityText
        }
    }
}




public List<LogMetrics> getLogMetricCount(List<String> serviceNameList, LocalDate from, LocalDate to, int minutesAgo) {
    System.out.println("from: " + from);
    System.out.println("to: " + to);
    System.out.println("minutesAgo: " + minutesAgo);

    List<LogDTO> logList = logQueryRepo.listAll();
    Map<String, LogMetrics> metricsMap = new HashMap<>();

    Instant fromInstant = null;
    Instant toInstant = null;

    if (from != null && to != null) {
        Instant startOfFrom = from.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant startOfTo = to.atStartOfDay(ZoneId.systemDefault()).toInstant();

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

    for (LogDTO logDTO : logList) {
        Date logCreateTime = logDTO.getCreatedTime();
        if (logCreateTime != null) {
            Instant logInstant = logCreateTime.toInstant();

            if (logInstant.isAfter(fromInstant) && logInstant.isBefore(toInstant)
                    && serviceNameList.contains(logDTO.getServiceName())) {
                String serviceName = logDTO.getServiceName();
                LogMetrics metrics = metricsMap.get(serviceName);

                if (metrics == null) {
                    metrics = new LogMetrics();
                    metrics.setServiceName(serviceName);
                    metrics.setErrorCallCount(0L);
                    metrics.setWarnCallCount(0L);
                    metrics.setDebugCallCount(0L);
                    metricsMap.put(serviceName, metrics);
                }

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
            if ("ERROR".equals(severityText) || "SEVERE".equals(severityText)) {
                metrics.setErrorCallCount(metrics.getErrorCallCount() + 1);
            } else if ("WARN".equals(severityText)) {
                metrics.setWarnCallCount(metrics.getWarnCallCount() + 1);
            } else if ("DEBUG".equals(severityText)) {
                metrics.setDebugCallCount(metrics.getDebugCallCount() + 1);
            }
        }
    }
}
public List<LogDTO> searchLogsPaged(int page, int pageSize, LocalDate from, LocalDate to) {
    System.out.println("from Date --------------" + from);
    System.out.println("to Date --------------" + to);

    // Swap 'from' and 'to' if 'to' is earlier than 'from'
    if (from != null && to != null && to.isBefore(from)) {
        LocalDate temp = from;
        from = to;
        to = temp;
    }

    FindIterable<Document> result = getFilteredResults( page, pageSize, from, to);

    List<LogDTO> logDTOList = new ArrayList<>();
    try (MongoCursor<Document> cursor = result.iterator()) {
        while (cursor.hasNext()) {
            Document document = cursor.next();
            LogDTO logDTO = new LogDTO();
            
            logDTO.setTraceId(document.getString("traceId"));
            logDTO.setServiceName(document.getString("serviceName"));
            logDTO.setCreatedTime(document.getDate("createdTime"));
            // Set other fields accordingly based on your document structure
            // ...

            logDTOList.add(logDTO);
        }
    }

    return logDTOList;
}

private FindIterable<Document> getFilteredResults(int page, int pageSize, LocalDate from, LocalDate to) {
    return null;
}

public long countQueryLogs(LocalDate from, LocalDate to) {
    // Swap 'from' and 'to' if 'to' is earlier than 'from'
    if (from != null && to != null && to.isBefore(from)) {
        LocalDate temp = from;
        from = to;
        to = temp;
    }

    FindIterable<Document> result = getFilteredResults( 0, Integer.MAX_VALUE, from, to);
    System.out.println("countQueryLogs: " + result.into(new ArrayList<>()).size());
    long totalCount = result.into(new ArrayList<>()).size();
    return totalCount;
}



}
   


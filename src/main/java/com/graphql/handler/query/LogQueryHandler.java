package com.graphql.handler.query;



import java.time.Instant;
import java.time.LocalDate;

import java.time.ZoneId;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;

import com.graphql.entity.otellog.ScopeLogs;
import com.graphql.entity.otellog.scopeLogs.LogRecord;
import com.graphql.entity.otellog.scopeLogs.Scope;
import com.graphql.entity.otellog.scopeLogs.logRecord.Body;
import com.graphql.entity.queryentity.log.LogDTO;

import com.graphql.repo.query.LogQueryRepo;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

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
                LogDTO existingLogDTO = logDTOMap.get(serviceName);

                if (existingLogDTO == null) {
                    existingLogDTO = new LogDTO();
                    // Copy relevant fields from logDTO to existingLogDTO
                    existingLogDTO.setServiceName(serviceName);
                    // Copy other fields as needed
                    logDTOMap.put(serviceName, existingLogDTO);
                }

                // Update existingLogDTO with logDTO values
                // (e.g., call some method like calculateCallCounts, if applicable)
                // calculateCallCounts(logDTO, existingLogDTO);
            }
        }
    }

    return new ArrayList<>(logDTOMap.values());
}



}



   


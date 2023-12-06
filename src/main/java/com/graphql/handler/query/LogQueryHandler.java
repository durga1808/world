package com.graphql.handler.query;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.graphql.entity.queryentity.log.LogDTO;
import com.graphql.entity.queryentity.log.LogQuery;
import com.graphql.repo.query.LogQueryRepo;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Field;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;



@ApplicationScoped
public class LogQueryHandler {
     



    @Inject
     LogQueryRepo logQueryRepo;



     @Inject
     MongoClient mongoClient;


    // public List<LogDTO> getAllLogs(){
    //     List<LogDTO> loglList =logQueryRepo.listAll();
    //     return loglList;
    // }
   
    // public List<LogDTO> getlogByServiceName(String serviceName) {
    //     return logQueryRepo.findByServiceName(serviceName);
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

public List<LogDTO> getFilterLogsByCreatedTimeDesc(List<LogDTO> logs) {
    System.out.println("------getFilterLogsByCreatedTimeDesc---------"+logs.size());
    return logs.stream()
.sorted(Comparator.comparing(LogDTO::getCreatedTime, Comparator.reverseOrder()))
.collect(Collectors.toList());

}


public List<LogDTO> getFilterLogssAsc(List<LogDTO> logs) {
   return logs.stream().sorted(Comparator.comparing(LogDTO::getCreatedTime)).collect(Collectors.toList());
}

public List<LogDTO> getFilterErrorLogs(List<LogDTO> logs) {
        return logs.stream()
            .sorted(Comparator
                .comparing((LogDTO log) -> {
                    String severityText = log.getSeverityText();
                    return ("ERROR".equals(severityText) || "SEVERE".equals(severityText)) ? 0 : 1;
                })
                .thenComparing(LogDTO::getCreatedTime, Comparator.nullsLast(Comparator.reverseOrder()))
            )
            .collect(Collectors.toList());
    }
   
}
  


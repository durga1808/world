package com.graphql.handler.query;
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
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;

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
   


//LogSummaryChartCount



    public List<LogMetrics> getLogMetricCount( List<String> serviceNameList, LocalDate from, LocalDate to,Integer minutesAgo
            
    ) {
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




    public List<LogDTO> getAllLogssOrderByCreatedTimeDesc(List<String> serviceNameList) {
        return logQueryRepo.findAllOrderByCreatedTimeDesc(serviceNameList);
    }

    public List<LogDTO> getAllLogssAsc(List<String> serviceNameList) {
        return logQueryRepo.findAllOrderByCreatedTimeAsc(serviceNameList);
    }

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


    public List<LogDTO>  searchFunction(String keyword ){
        return logQueryRepo.searchByStringValue(keyword);
    }




    // public List<LogDTO> filterServiceName(LogQuery logQuery) {

        
    //     PanacheQuery<LogDTO> panacheQuery = LogDTO.find(
    //             "serviceName in :serviceNames and severityText in :severityTexts",
    //             Parameters
    //                     .with("serviceNames", logQuery.getServiceName())
    //                     .and("severityTexts", logQuery.getSeverityText()));
    
    //     return panacheQuery.list();
    // }
   
 
       public List<LogDTO> filterServiceName(List<String> serviceNames, List<String> severityTexts, LocalDate fromDate, LocalDate toDate) {

        // Convert from LocalDate to Date with IST time zone
        ZoneId istZone = ZoneId.of("Asia/Kolkata");
        Instant fromInstant = fromDate.atStartOfDay(istZone).toInstant();
        Instant toInstant = toDate.plusDays(1).atStartOfDay(istZone).toInstant();

        PanacheQuery<LogDTO> panacheQuery = LogDTO.find(
                "serviceName in :serviceNames and severityText in :severityTexts " +
                        "and createdTime between :fromDate and :toDate",
                Parameters
                        .with("serviceNames", serviceNames)
                        .and("severityTexts", severityTexts)
                        .and("fromDate", Date.from(fromInstant))
                        .and("toDate", Date.from(toInstant)));

        return panacheQuery.list();
    }


}
package com.graphql.repo.query;


import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.graphql.entity.queryentity.log.LogDTO;
import com.graphql.entity.queryentity.log.LogPage;
import com.graphql.entity.queryentity.log.LogQuery;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;



@ApplicationScoped
public class LogQueryRepo implements PanacheMongoRepository<LogDTO> {
    
public List<LogDTO> findByServiceName(String serviceName) {
        return list("serviceName", serviceName);

 
}

public List<LogDTO> findAllOrderByCreatedTimeDesc(List<String> serviceNameList) {
        return find("serviceName in ?1",Sort.descending("createdTime"),serviceNameList).list();
    }

    public List<LogDTO> findAllOrderByCreatedTimeAsc(List<String> serviceNameList) {
        return find("serviceName in ?1",Sort.ascending("createdTime"),serviceNameList).list();
    }


    // public List<LogDTO> searchByStringValue(String keyword) {
       
    //     return find("{'scopeLogs.logRecords.body.stringValue': { $regex: ?1, $options: 'i' }}", keyword).list();
    // }

    
// public LogPage searchFunction(String keyword, int page, int pageSize, LocalDate fromDate, LocalDate toDate, Integer minutesAgo) {
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

//     return find("{'scopeLogs.logRecords.body.stringValue': { $regex: ?1, $options: 'i' }, " +
//                 "'createdTime': { $gte: ?2, $lte: ?3 }}", keyword, fromInstant, toInstant)

            
//         int totalCount = logs.size();
//         int startIdx = (page - 1) * pageSize;
//     int endIdx = Math.min(startIdx + pageSize,  logs.size());
//     List<LogDTO> paginatedLogs =  logs.subList(startIdx, endIdx);

//     // Create and return LogPage object
//     return new LogPage(paginatedLogs, totalCount);    
//             .page(Page.of(page, pageSize))
//             .list();
// }

public LogPage searchFunction(String keyword, int page, int pageSize, LocalDate fromDate, LocalDate toDate, Integer minutesAgo) {
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

    // Find logs based on the search criteria
    List<LogDTO> logs = find("{'scopeLogs.logRecords.body.stringValue': { $regex: ?1, $options: 'i' }, " +
                "'createdTime': { $gte: ?2, $lte: ?3 }}", keyword, fromInstant, toInstant)
            .list();

    // Calculate total count
    int totalCount = logs.size();

    // Calculate start and end indices for pagination
    int startIdx = (page - 1) * pageSize;
    int endIdx = Math.min(startIdx + pageSize, logs.size());

    // Get the sublist for the current page
    List<LogDTO> paginatedLogs = logs.subList(startIdx, endIdx);

    // Create and return LogPage object with total count
    return new LogPage(paginatedLogs, totalCount);
}





//  public List<LogDTO> FilterLogsByCreatedTimeDesc(List<LogDTO> logs) {
//         System.out.println("------getFilterLogsByCreatedTimeDesc---------" + logs.size());
//         return logs.stream()
//                 .sorted(Comparator.comparing(LogDTO::getCreatedTime, Comparator.reverseOrder()))
//                 .collect(Collectors.toList());
//     }

   
//     public List<LogDTO> getFilterLogssAsc(List<LogDTO> logs) {
//         return logs.stream()
//                 .sorted(Comparator.comparing(LogDTO::getCreatedTime))
//                 .collect(Collectors.toList());
//     }

//     public List<LogDTO> getFilterErrorLogs(List<LogDTO> logs) {
//         return logs.stream()
//                 .sorted(Comparator
//                         .comparing((LogDTO log) -> {
//                             String severityText = log.getSeverityText();
//                             return ("ERROR".equals(severityText) || "SEVERE".equals(severityText)) ? 0 : 1;
//                         })
//                         .thenComparing(LogDTO::getCreatedTime, Comparator.nullsLast(Comparator.reverseOrder()))
//                 )
//                 .collect(Collectors.toList());
//     }




public List<LogDTO> findOrderByCreatedTimeDesc(List<String> serviceNameList) {
    return find("serviceName in ?1",Sort.descending("createdTime"),serviceNameList).list();
}

public List<LogDTO> findOrderByCreatedTimeAsc(List<String> serviceNameList) {
    return find("serviceName in ?1",Sort.ascending("createdTime"),serviceNameList).list();
}


}



 
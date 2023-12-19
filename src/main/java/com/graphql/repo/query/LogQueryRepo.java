package com.graphql.repo.query;


import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;


import com.graphql.entity.queryentity.log.LogDTO;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;



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

    
public List<LogDTO> searchFunction(String keyword, int page, int pageSize, LocalDate fromDate, LocalDate toDate, Integer minutesAgo) {
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

    return find("{'scopeLogs.logRecords.body.stringValue': { $regex: ?1, $options: 'i' }, " +
                "'createdTime': { $gte: ?2, $lte: ?3 }}", keyword, fromInstant, toInstant)
            .page(Page.of(page, pageSize))
            .list();
}


 }
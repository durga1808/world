package com.graphql.repo.query;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.graphql.entity.queryentity.metric.MetricDTO;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
@ApplicationScoped
public class MetricQueryRepo  implements PanacheMongoRepository<MetricDTO>{
    


// public List<MetricDTO> findByServiceName(String serviceName) {
//         return list("serviceName", serviceName);
//     }
    
// public List<MetricDTO> findByServiceName(String serviceName) {
//     return find("{'serviceName': { $regex: ?1, $options: 'i' }}", serviceName).list();
// }


public List<MetricDTO> findByServiceName(String serviceName, LocalDate fromDate, LocalDate toDate, Integer minutesAgo) {
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

    return find("{'serviceName': { $regex: ?1, $options: 'i' }, " +
                "'date': { $gte: ?2, $lte: ?3 }}", serviceName, fromInstant, toInstant)
            .list();
}


}

package com.graphql.controller.query;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;


import com.graphql.entity.queryentity.log.LogDTO;
import com.graphql.entity.queryentity.log.LogMetrics;
import com.graphql.entity.queryentity.log.LogQuery;

import com.graphql.handler.query.LogQueryHandler;
import com.graphql.repo.query.LogQueryRepo;

import jakarta.inject.Inject;



@GraphQLApi
public class LogQuerycontroller{
    
 @Inject
 LogQueryHandler logQueryHandler;
 
 @Inject 
 LogQueryRepo logQueryRepo;




   @Query("getAllLogsData")
   public List<LogDTO> getAllLogData(){
       return logQueryHandler.getAllLogs();
   }
   
   @Query
   public List<LogDTO> getlogByServiceName(@Name("serviceName") String serviceName) {
    // Call the repository method to perform the search
    return logQueryHandler.getlogByServiceName(serviceName);
    }


   

  




// @Query("logsDateAndTime")

// public List<LogMetrics> getLogMetricsCount(
//     @QueryParam("startDate") LocalDate endDate,
//     @QueryParam("endDate") LocalDate startDate,
//     @QueryParam("serviceNameList") List<String> serviceNameList,
//     @QueryParam("minutesAgo") int minutesAgo
// ) {
//     return logQueryHandler.getLogMetricCount(serviceNameList, endDate, startDate, minutesAgo);
// }




@Query("searchLogPaged")
  public List<LogDTO> searchLogsPaged(
    LogQuery logQuery,
    int page,
    int pageSize,
    LocalDate from,
    LocalDate to,
    Integer minutesAgo
  ) {


    
    return logQueryHandler.searchLogsPaged(logQuery, from, to, minutesAgo);
    
  }
    
}

  
  






   

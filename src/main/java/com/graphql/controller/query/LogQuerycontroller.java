package com.graphql.controller.query;


import java.time.LocalDate;

import java.util.List;


import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;


import com.graphql.entity.queryentity.log.LogDTO;
import com.graphql.entity.queryentity.log.LogMetrics;
import com.graphql.entity.queryentity.trace.TraceDTO;
import com.graphql.entity.queryentity.trace.TraceQuery;
import com.graphql.handler.query.LogQueryHandler;
import com.graphql.repo.query.LogQueryRepo;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;


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


   

  
// public List<LogDTO> searchLogs(
//         @QueryParam("startDate") LocalDate from,
//         @QueryParam("endDate") LocalDate to,
//         @QueryParam("minutesAgo") int minutesAgo) throws JsonProcessingException  {

    
//         List<LogDTO> logList = new ArrayList<>();

//         if (from != null && to != null) {
//             // Your existing date range filtering logic
//             // ...

//         } else if (minutesAgo > 0) {
//             // Your existing minutes ago filtering logic
//             // ...
//         }

//         // Additional filtering or sorting if needed
//         // ...

//         int totalCount = logList.size();

//         Map<String, Object> response = new HashMap<>();
//         response.put("data", logList);
//         response.put("totalCount", totalCount);

//         ObjectMapper objectMapper = new ObjectMapper();
//         String responseJson = objectMapper.writeValueAsString(response);
//         return logList;


//   @Query("logsDateAndTime")

// public List<LogDTO> getLogsDateTime(
//     @QueryParam("startDate") LocalDate endDate,
//     @QueryParam("endDate") LocalDate startDate,
    
//     @QueryParam("minutesAgo") int minutesAgo
// ) {
//     List<LogDTO> logDTOList = logQueryHandler.getLogDTOList(endDate, startDate, minutesAgo);
//     return logDTOList;
// }
   

// public List<LogDTO> getByServiceName(
//         @QueryParam("from") LocalDate from,
//         @QueryParam("to") LocalDate to,
//         @QueryParam("serviceName") String serviceName,
//         @QueryParam("minutesAgo") int minutesAgo
// ) {
//     List<LogDTO> logData = logQueryHandler.getLogData(from, to, serviceName, minutesAgo);
//     return logData;
// }


@Query("logsUsingDateAndTime")

public List<LogDTO> getLogsDateAndTime(
    @QueryParam("startDate") LocalDate endDate,
    @QueryParam("endDate") LocalDate startDate,
    @QueryParam("serviceNameList") List<String> serviceNameList,
    @QueryParam("minutesAgo") int minutesAgo
) {
    List<LogDTO> logDTOList = logQueryHandler.getLogDTOList(serviceNameList, endDate, startDate, minutesAgo);                //conroller
    return logDTOList;
}

  




@Query("logsDateAndTime")

public List<LogMetrics> getLogMetricsCount(
    @QueryParam("startDate") LocalDate endDate,
    @QueryParam("endDate") LocalDate startDate,
    @QueryParam("serviceNameList") List<String> serviceNameList,
    @QueryParam("minutesAgo") int minutesAgo
) {
    return logQueryHandler.getLogMetricCount(serviceNameList, endDate, startDate, minutesAgo);
}




@Query("searchLogPaged")
  public List<LogDTO> searchLogsPaged(
    
    int page,
    int pageSize,
    LocalDate from,
    LocalDate to
    //int minutesAgo
  ) {
    return logQueryHandler.searchLogsPaged( page, pageSize, from, to);
    
  }

    
}

  
  






   

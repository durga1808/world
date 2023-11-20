package com.graphql.controller.query;


import java.time.LocalDate;


import java.util.List;


import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;


import com.graphql.entity.queryentity.log.LogDTO;

import com.graphql.handler.query.LogQueryHandler;
import com.graphql.repo.query.LogQueryRepo;

import jakarta.inject.Inject;

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


   
//   @Query("logsDateAndTime")
  
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



@Query("logsDateAndTime")

public List<LogDTO> getLogsDateTime(
    @QueryParam("startDate") LocalDate endDate,
    @QueryParam("endDate") LocalDate startDate,
    @QueryParam("serviceNameList") List<String> serviceNameList,
    @QueryParam("minutesAgo") int minutesAgo
) {
    List<LogDTO> logDTOList = logQueryHandler.getLogDTOList(serviceNameList, endDate, startDate, minutesAgo);
    return logDTOList;
}
   
      
    
}

  
  






   

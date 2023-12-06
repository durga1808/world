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
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;



@GraphQLApi
public class LogQuerycontroller{
    
 @Inject
 LogQueryHandler logQueryHandler;
 
 @Inject 
 LogQueryRepo logQueryRepo;




  //  @Query("getAllLogsData")
  //  public List<LogDTO> getAllLogData(){
  //      return logQueryHandler.getAllLogs();
  //  }
   
  //  @Query
  //  public List<LogDTO> getlogByServiceName(@Name("serviceName") String serviceName) {
  //   // Call the repository method to perform the search
  //   return logQueryHandler.getlogByServiceName(serviceName);
  //   }



@Query("searchLogPaged")
public List<LogDTO> searchLogsPaged(
        LogQuery logQuery,
        int page,
        int pageSize,
        LocalDate from,
        LocalDate to,
        Integer minutesAgo,
        String sortOrder
) {
    List<LogDTO> logs = logQueryHandler.searchLogsPaged(logQuery, from, to, minutesAgo);

    if ("new".equalsIgnoreCase(sortOrder)) {
      logs = logQueryHandler.getFilterLogsByCreatedTimeDesc(logs);
  } else if ("old".equalsIgnoreCase(sortOrder)) {
      logs = logQueryHandler.getFilterLogssAsc(logs);
  } else if ("error".equalsIgnoreCase(sortOrder)) {
      logs = logQueryHandler.getFilterErrorLogs(logs);
  }  else{
    throw new IllegalArgumentException("Invalid sortOrder parameter. Use 'new', 'old', or 'error'.");
}

    return logs;
}




//   @POST
// @Path("/filterLogs")
// @Consumes("application/json")
// @Produces("application/json")
// public Response filterLogs(
//         LogQuery logQuery,
//         @QueryParam("page") @DefaultValue("1") int page,
//         @QueryParam("pageSize") @DefaultValue("10") int pageSize,
//         @QueryParam("startDate") LocalDate from,
//         @QueryParam("endDate") LocalDate to,
//         @QueryParam("minutesAgo") int minutesAgo,
//         @QueryParam("sortOrder") String sortOrder) {



//         }

  
    
}

  
  






   

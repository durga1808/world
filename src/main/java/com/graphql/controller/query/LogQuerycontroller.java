package com.graphql.controller.query;


import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;


import com.graphql.entity.queryentity.log.LogDTO;
import com.graphql.entity.queryentity.log.LogMetrics;
import com.graphql.entity.queryentity.log.LogPage;
import com.graphql.entity.queryentity.log.LogQuery;

import com.graphql.handler.query.LogQueryHandler;
import com.graphql.repo.query.LogQueryRepo;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;



@GraphQLApi

@Path("/logs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)

public class LogQuerycontroller{
    
 @Inject
 LogQueryHandler logQueryHandler;
 
 @Inject 
 LogQueryRepo logQueryRepo;




   @Query("getAllLogsData")
   public List<LogDTO> getAllLogData(){
       return logQueryHandler.getAllLogs();
   }
   
  //  @Query
  //  public List<LogDTO> getlogByServiceName(@Name("serviceName") String serviceName) {
  //   // Call the repository method to perform the search
  //   return logQueryHandler.getlogByServiceName(serviceName);
  //   }




// @POST
//     @Path("/filteredLogs")
//     @Consumes("application/json")
//     @Produces("application/json")
//     public Response filterLogs(
//             LogQuery logQuery,
//             @QueryParam("page") @DefaultValue("1") int page,
//             @QueryParam("pageSize") @DefaultValue("10") int pageSize,
//             @QueryParam("startDate") LocalDate from,
//             @QueryParam("endDate") LocalDate to,
//             @QueryParam("minutesAgo") int minutesAgo
//             // @QueryParam("sortOrder") String sortOrder
//             ) {

//         // Build your GraphQL query
//         String graphqlQuery = buildGraphQLQuery(logQuery, page, pageSize, from, to, minutesAgo
//         // , sortOrder
//         );

//         // Execute the GraphQL query
//         String apiUrl = "http://localhost:5002/graphql";
//         String requestBody = "" + graphqlQuery + "";

//         HttpClient httpClient = HttpClient.newHttpClient();
//         HttpRequest request = HttpRequest.newBuilder()
//                 .uri(URI.create(apiUrl))
//                 .header("Content-Type", "application/json")
//                 .POST(HttpRequest.BodyPublishers.ofString(requestBody))
//                 .build();


//                 System.out.println("Sending GraphQL Request to: " + apiUrl);
//                 System.out.println("Request Body: " + requestBody);

//         try {
//             HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

//             if (response.statusCode() == 200) {
//                 // Successful execution
//                 String responseData = response.body(); // Your GraphQL response data
//                 return Response.ok(responseData).build();
//             } else {
//                 // Handle GraphQL execution errors
//                 return Response.status(response.statusCode())
//                         .entity("GraphQL execution failed: " + response.body())
//                         .build();
//             }
//         } catch (Exception e) {
//             // Handle exceptions
//             return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
//                     .entity("Error executing GraphQL query: " + e.getMessage())
//                     .build();
//         }
//     }  
    
    
    
//     private String buildGraphQLQuery(LogQuery logQuery, int page, int pageSize, LocalDate from, LocalDate to, int minutesAgo
//     // , String sortOrder
//     ) {
//       return String.format("query {" +
//                       "  searchLogPaged(" +
//                       "    from: %s," +
//                       "    page: %d," +
//                       "    pageSize: %d," +
//                       "    minutesAgo: %s," +
//                       "    to: %s," +
//                       "    logQuery: %s" +
//                       "  ) {" +
//                       "    createdTime" +
//                       "    serviceName" +
//                       "    severityText" +
//                       "    spanId" +
//                       "    traceId" +
//                       "    scopeLogs {" +
//                       "      scope {" +
//                       "        name" +
//                       "      }" +
//                       "      logRecords {" +
//                       "        attributes {" +
//                       "          key" +
//                       "          value {" +
//                       "            intValue" +
//                       "            stringValue" +
//                       "          }" +
//                       "        }" +
//                       "        body {" +
//                       "          stringValue" +
//                       "        }" +
//                       "        flags" +
//                       "        observedTimeUnixNano" +
//                       "        severityNumber" +
//                       "        severityText" +
//                       "        spanId" +
//                       "        timeUnixNano" +
//                       "        traceId" +
//                       "      }" +
//                       "    }" +
//                       "  }" +
//                       "}",
//               quoteIfNotNull(from != null ? from.toString() : null),
//               page,
//               pageSize,
//               minutesAgo >= 0 ? minutesAgo : "null",
//               quoteIfNotNull(to != null ? to.toString() : null),
//               logQueryToGraphQLInput(logQuery));
//   }
  
//   private String quoteIfNotNull(Object value) {
//       return value != null ? "\"" + value.toString() + "\"" : "null";
//   }
  
//   private String logQueryToGraphQLInput(LogQuery logQuery) {
//     System.out.println("Service Name List: " + logQuery.getServiceName());
//     System.out.println("Severity Text List: " + logQuery.getSeverityText());

//     return "{"
//             + " serviceName: " + convertListToGraphQLArray(logQuery.getServiceName()) + ","
//             + " severityText: " + convertListToGraphQLArray(logQuery.getSeverityText())
//             + "}";
// }


// private String convertListToGraphQLArray(List<String> list) {
//   if (list == null || list.isEmpty()) {
//       return "[]";
//   }

//   StringBuilder arrayBuilder = new StringBuilder("[");
//   for (String item : list) {
//       arrayBuilder.append("\"").append(item).append("\",");
//   }
//   arrayBuilder.deleteCharAt(arrayBuilder.length() - 1); // Remove the trailing comma
//   arrayBuilder.append("]");

//   return arrayBuilder.toString();
// }


@Query
public LogPage filterLogs(
    @Name("query") LogQuery query,
    @Name("page") int page,
    @Name("pageSize") int pageSize,
    @Name("from") LocalDate fromDate,
    @Name("to") LocalDate toDate,
    @Name("minutesAgo") Integer minutesAgo,
    @Name("sortOrder") String sortOrder) {
  


    List<LogDTO> logDTOs = logQueryHandler.filterServiceName(query, fromDate, toDate, minutesAgo, sortOrder);
           

            if ("new".equalsIgnoreCase(sortOrder)) {
                logDTOs = logQueryHandler.filterLogsByCreatedTimeDesc(logDTOs);
            } else if ("old".equalsIgnoreCase(sortOrder)) {
                logDTOs = logQueryHandler.filterLogsAsc(logDTOs);
            } else if ("error".equalsIgnoreCase(sortOrder)) {
                logDTOs = logQueryHandler.filterErrorLogs(logDTOs);
            } else {
                throw new IllegalArgumentException("Invalid sortOrder parameter. Use 'new', 'old', or 'error'.");
            }

        int totalCount = logDTOs.size();
        int startIdx = (page - 1) * pageSize;
    int endIdx = Math.min(startIdx + pageSize,  logDTOs.size());
// System.out.println("----------filter log size"+ logDTOs.size());
    List<LogDTO> paginatedLogs =  logDTOs.subList(startIdx, endIdx);
    return new LogPage(paginatedLogs, totalCount);
    }
   
     


@Query
public List<LogDTO> searchFunction(@Name("keyword") String keyword, 
@Name("page") int page,
        @Name("pageSize") int pageSize,
         @Name("from") LocalDate fromDate,
        @Name("to") LocalDate toDate,
        @Name("minutesAgo") Integer minutesAgo) {
          return logQueryHandler.searchFunction(keyword,page,pageSize,fromDate,toDate,minutesAgo);
        }


@Query
public List<LogMetrics> logMetricsCount(
    @Name("startDate") LocalDate startDate,
    @Name("endDate") LocalDate endDate,
    @Name("serviceNameList") List<String> serviceNameList,
    @Name("minutesAgo") int minutesAgo
) {
    // Assuming logQueryHandler has a method similar to getLogMetricCount
    return logQueryHandler.getLogMetricCount(minutesAgo, startDate, endDate, serviceNameList);
}

@Query
public LogPage sortOrderLogs(
        @Name("sortOrder") String sortOrder,
        @Name("serviceNameList") List<String> serviceNameList,
        @Name("page") int page,
        @Name("pageSize") int pageSize,
        @Name("from") LocalDate fromDate,
        @Name("to") LocalDate toDate,
        @Name("minutesAgo") Integer minutesAgo
) {
    List<LogDTO> logs;

    if ("new".equalsIgnoreCase(sortOrder)) {
        logs = logQueryHandler.getAllLogssOrderByCreatedTimeDesc(serviceNameList);
    } else if ("old".equalsIgnoreCase(sortOrder)) {
        logs = logQueryHandler.getAllLogssAsc(serviceNameList);
    } else if ("error".equalsIgnoreCase(sortOrder)) {
        logs = logQueryHandler.getErrorLogsByServiceNamesOrderBySeverityAndCreatedTimeDesc(serviceNameList);
    } else {
        logs = new ArrayList<>();
    }

    Instant fromInstant;
    Instant toInstant;

    if (fromDate != null && toDate != null) {
        fromInstant = fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        toInstant = toDate.atStartOfDay(ZoneId.systemDefault()).toInstant().plus(1, ChronoUnit.DAYS);
    } else if (minutesAgo != null && minutesAgo > 0) {
        Instant currentInstant = Instant.now();
        Instant minutesAgoInstant = currentInstant.minus(minutesAgo, ChronoUnit.MINUTES);

        fromInstant = minutesAgoInstant.isBefore(currentInstant) ? minutesAgoInstant : currentInstant;
        toInstant = currentInstant;
    } else {
        throw new IllegalArgumentException("Either date range or minutesAgo must be provided");
    }

    logs = logs.stream()
            .filter(log -> {
                Date createdDate = log.getCreatedTime();
                if (createdDate != null) {
                    Instant createdInstant = createdDate.toInstant();
                    return createdInstant.isAfter(fromInstant) && createdInstant.isBefore(toInstant);
                }
                return false;
            })
            .collect(Collectors.toList());

   
        int totalCount = logs.size();
        int startIdx = (page - 1) * pageSize;
    int endIdx = Math.min(startIdx + pageSize,  logs.size());
    List<LogDTO> paginatedLogs =  logs.subList(startIdx, endIdx);

    // Create and return LogPage object
    return new LogPage(paginatedLogs, totalCount);


}





}



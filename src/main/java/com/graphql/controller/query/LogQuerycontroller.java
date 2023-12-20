package com.graphql.controller.query;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;



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




// @Query
// public LogPage searchFunction( String keyword, 
// LocalDate from,
//  LocalDate to, 
//  Integer minutesAgo,
//  int page,
//  int pageSize ){

//      List<LogDTO> log = logQueryHandler.searchFunction(keyword);

//      if (from != null && to != null) {
        
//         if (from.isAfter(to)) {
//             LocalDate temp = from;
//             from = to;
//             to = temp;
//         }


//         Instant fromInstant = from.atStartOfDay(ZoneId.systemDefault()).toInstant();
//         Instant toInstant = to.atStartOfDay(ZoneId.systemDefault()).toInstant().plus(1, ChronoUnit.DAYS);

//         log = filterLogsByDateRange(log, fromInstant, toInstant);
//     } else if (minutesAgo > 0) {
//         Instant currentInstant = Instant.now();
//         Instant fromInstant = currentInstant.minus(minutesAgo, ChronoUnit.MINUTES);
//         Instant toInstant = currentInstant.minus(1, ChronoUnit.MINUTES);

//         log = filterLogsByMinutesAgo(log, fromInstant, toInstant);
//     }
//     int totalCount = log.size();

 
//     int startIdx = (page - 1) * pageSize;
//     int endIdx = Math.min(startIdx + pageSize, log.size());
//     List<LogDTO> paginatedLogs = log.subList(startIdx, endIdx);

 
//     return new LogPage(paginatedLogs, totalCount);

// }

// private List<LogDTO> filterLogsByDateRange(List<LogDTO> logs, Instant from, Instant to) {
//     return logs.stream()
//             .filter(log -> isWithinDateRange(log.getCreatedTime(), from, to))
//             .collect(Collectors.toList());
// }

// private List<LogDTO> filterLogsByMinutesAgo(List<LogDTO> logs, Instant fromInstant, Instant toInstant) {
   
//     final Instant startOfCurrentDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();

//     Instant adjustedFromInstant = fromInstant.isBefore(startOfCurrentDay) ? startOfCurrentDay : fromInstant;

//     return logs.stream()
//             .filter(log -> isWithinDateRange(log.getCreatedTime(), adjustedFromInstant, toInstant))
//             .collect(Collectors.toList());
// }



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
    


// @Query
// public List<LogDTO> filterLogs(
//         @Name("query") LogQuery query,
//         @Name("page") int page,
//         @Name("pageSize") int pageSize,
//         @Name("from") LocalDate fromDate,
//         @Name("to") LocalDate toDate,
//         @Name("minutesAgo") Integer minutesAgo,
//         @Name("sortorder") String sortOrder) {

//     List<LogDTO> logs = logQueryHandler.filterServiceName(query, page, pageSize, fromDate, toDate, minutesAgo);

//     if (sortOrder != null) {
//         if ("new".equalsIgnoreCase(sortOrder)) {
//             logs = logQueryHandler.getFilterLogsByCreatedTimeDesc(logs);
//         } else if ("old".equalsIgnoreCase(sortOrder)) {
//             logs = logQueryHandler.getFilterLogssAsc(logs);
//         } else if ("error".equalsIgnoreCase(sortOrder)) {
//             logs = logQueryHandler.getFilterErrorLogs(logs);
//         }
//     }

//     return logs;
// }


@Query
public List<LogDTO> filterLogs(
    @Name("query") LogQuery query,
    @Name("page") int page,
    @Name("pageSize") int pageSize,
    @Name("from") LocalDate fromDate,
    @Name("to") LocalDate toDate,
    @Name("minutesAgo") Integer minutesAgo,
    @Name("sortorder") String sortOrder) {
     
    


        List<LogDTO> logDTOs = logQueryHandler.filterServiceName(query, page, pageSize, fromDate, toDate, minutesAgo);
        List<LogDTO> logDTOs2 = new ArrayList<>();
        
        if (sortOrder != null) {
            switch (sortOrder.trim().toLowerCase()) {
                case "new":
                    logDTOs2 = logDTOs.stream()
                            .sorted(Comparator.comparing(LogDTO::getCreatedTime).reversed())
                            .collect(Collectors.toList());
                    break;
                case "old":
                    logDTOs2 = logDTOs.stream()
                            .sorted(Comparator.comparing(LogDTO::getCreatedTime))
                            .collect(Collectors.toList());
                    break;
                case "error":
                    Comparator<LogDTO> errorComparator = Comparator
                            .comparing((LogDTO log) -> {
                                String severity = log.getSeverityText();
                                return ("ERROR".equals(severity) || "SEVERE".equals(severity)) ? 0 : 1;
                            })
                            .thenComparing(LogDTO::getCreatedTime, Comparator.nullsLast(Comparator.reverseOrder()));
        
                    logDTOs2 = logDTOs.stream()
                            .sorted(errorComparator)
                            .collect(Collectors.toList());
                    break;
                default:
                    // Handle invalid sortOrder (optional)
                    throw new IllegalArgumentException("Invalid sortOrder: " + sortOrder);
            }
        } else {
            logDTOs2 = logDTOs; // No sorting needed, keep the original order
        }
        
        return logDTOs2;
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





}



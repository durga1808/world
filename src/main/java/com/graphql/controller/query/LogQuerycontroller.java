package com.graphql.controller.query;


import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.naming.directory.SearchResult;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphql.entity.queryentity.log.LogDTO;
import com.graphql.entity.queryentity.log.LogMetrics;
import com.graphql.entity.queryentity.log.LogPage;
import com.graphql.entity.queryentity.log.LogQuery;

import com.graphql.handler.query.LogQueryHandler;
import com.graphql.repo.query.LogQueryRepo;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
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




  //  @Query("getAllLogsData")
  //  public List<LogDTO> getAllLogData(){
  //      return logQueryHandler.getAllLogs();
  //  }
   
  //  @Query
  //  public List<LogDTO> getlogByServiceName(@Name("serviceName") String serviceName) {
  //   // Call the repository method to perform the search
  //   return logQueryHandler.getlogByServiceName(serviceName);
  //   }



// @Query("filterLogs")
// public List<LogDTO> searchLogsPaged(
//         LogQuery logQuery,
//         int page,
//         int pageSize,
//         LocalDate from,
//         LocalDate to,
//         Integer minutesAgo,
//         String sortOrder
// ) {
//     List<LogDTO> logs = logQueryHandler.searchLogsPaged(logQuery, from, to, minutesAgo);

//     if ("new".equalsIgnoreCase(sortOrder)) {
//       logs = logQueryHandler.getFilterLogsByCreatedTimeDesc(logs);
//   } else if ("old".equalsIgnoreCase(sortOrder)) {
//       logs = logQueryHandler.getFilterLogssAsc(logs);
//   } else if ("error".equalsIgnoreCase(sortOrder)) {
//       logs = logQueryHandler.getFilterErrorLogs(logs);
//   }  else{
//     throw new IllegalArgumentException("Invalid sortOrder parameter. Use 'new', 'old', or 'error'.");
// }

//     return logs;
// }




@Query("filterLogs")
public LogPage searchLogsPaged(
        LogQuery logQuery,
        int page,
        int pageSize,
        LocalDate from,
        LocalDate to,
        Integer minutesAgo,
        String sortOrder
) {
    // Get the total count and paginated logs
    List<LogDTO> logs = logQueryHandler.searchLogsPaged(logQuery, from, to, minutesAgo);
    

    // Apply sorting based on sortOrder
    if ("new".equalsIgnoreCase(sortOrder)) {
        logs = logQueryHandler.getFilterLogsByCreatedTimeDesc(logs);
    } else if ("old".equalsIgnoreCase(sortOrder)) {
        logs = logQueryHandler.getFilterLogssAsc(logs);
    } else if ("error".equalsIgnoreCase(sortOrder)) {
        logs = logQueryHandler.getFilterErrorLogs(logs);
    } else {
        throw new IllegalArgumentException("Invalid sortOrder parameter. Use 'new', 'old', or 'error'.");
    }

    // Calculate total count
    int totalCount = logs.size();

    // Apply pagination
    int startIdx = (page - 1) * pageSize;
    int endIdx = Math.min(startIdx + pageSize, logs.size());
    List<LogDTO> paginatedLogs = logs.subList(startIdx, endIdx);

    // Create and return LogPage object
    return new LogPage(paginatedLogs, totalCount);
}


// @Query
// public List<LogDTO> searchLogsPaged(
//         LogQuery logQuery,
//         int page,
//         int pageSize,
//         LocalDate from,
//         LocalDate to,
//         Integer minutesAgo,
//         String sortOrder
// ) {
//     if (page <= 0 || pageSize <= 0) {
//         // Returning an empty list if page or pageSize is invalid
//         return Collections.emptyList();
//     }

//     try {
//         List<LogDTO> logs = logQueryHandler.searchLogsPaged(logQuery, from, to, minutesAgo);

//         if ("new".equalsIgnoreCase(sortOrder)) {
//             logs = logQueryHandler.getFilterLogsByCreatedTimeDesc(logs);
//         } else if ("old".equalsIgnoreCase(sortOrder)) {
//             logs = logQueryHandler.getFilterLogssAsc(logs);
//         } else if ("error".equalsIgnoreCase(sortOrder)) {
//             logs = logQueryHandler.getFilterErrorLogs(logs);
//         } else {
//             throw new IllegalArgumentException("Invalid sortOrder parameter. Use 'new', 'old', or 'error'.");
//         }

//         int startIndex = (page - 1) * pageSize;
//         int endIndex = Math.min(startIndex + pageSize, logs.size());

//         if (startIndex >= endIndex || logs.isEmpty()) {
//             Map<String, Object> emptyResponse = new HashMap<>();
//             // emptyResponse.put("data", Collections.emptyList());
//             // emptyResponse.put("totalCount", emptyResponse.size());
//             emptyResponse.put("totalCount", logs.size());

            
//             // Returning an empty list if no logs are found
//             return buildResponse(emptyResponse);
//         }
       
//         // Returning the sublist of logs with total count
//         return logs.subList(startIndex, endIndex);
//     } catch (Exception e) {
//         // Handle exceptions if necessary
//         return Collections.emptyList();
//     }
// }

// private List<LogDTO> buildResponse(Map<String, Object> response) {
//     // Implement the logic to convert the response map into a List<LogDTO>
//     // ...

//     // For now, returning an empty list as a placeholder
//     return Collections.emptyList();
// }



@POST
    @Path("/filteredLogs")
    @Consumes("application/json")
    @Produces("application/json")
    public Response filterLogs(
            LogQuery logQuery,
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize,
            @QueryParam("startDate") LocalDate from,
            @QueryParam("endDate") LocalDate to,
            @QueryParam("minutesAgo") int minutesAgo
            // @QueryParam("sortOrder") String sortOrder
            ) {

        // Build your GraphQL query
        String graphqlQuery = buildGraphQLQuery(logQuery, page, pageSize, from, to, minutesAgo
        // , sortOrder
        );

        // Execute the GraphQL query
        String apiUrl = "http://localhost:5002/graphql";
        String requestBody = "" + graphqlQuery + "";

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();


                System.out.println("Sending GraphQL Request to: " + apiUrl);
                System.out.println("Request Body: " + requestBody);

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // Successful execution
                String responseData = response.body(); // Your GraphQL response data
                return Response.ok(responseData).build();
            } else {
                // Handle GraphQL execution errors
                return Response.status(response.statusCode())
                        .entity("GraphQL execution failed: " + response.body())
                        .build();
            }
        } catch (Exception e) {
            // Handle exceptions
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error executing GraphQL query: " + e.getMessage())
                    .build();
        }
    }  
    
    
    
    private String buildGraphQLQuery(LogQuery logQuery, int page, int pageSize, LocalDate from, LocalDate to, int minutesAgo
    // , String sortOrder
    ) {
      return String.format("query {" +
                      "  searchLogPaged(" +
                      "    from: %s," +
                      "    page: %d," +
                      "    pageSize: %d," +
                      "    minutesAgo: %s," +
                      "    to: %s," +
                      "    logQuery: %s" +
                      "  ) {" +
                      "    createdTime" +
                      "    serviceName" +
                      "    severityText" +
                      "    spanId" +
                      "    traceId" +
                      "    scopeLogs {" +
                      "      scope {" +
                      "        name" +
                      "      }" +
                      "      logRecords {" +
                      "        attributes {" +
                      "          key" +
                      "          value {" +
                      "            intValue" +
                      "            stringValue" +
                      "          }" +
                      "        }" +
                      "        body {" +
                      "          stringValue" +
                      "        }" +
                      "        flags" +
                      "        observedTimeUnixNano" +
                      "        severityNumber" +
                      "        severityText" +
                      "        spanId" +
                      "        timeUnixNano" +
                      "        traceId" +
                      "      }" +
                      "    }" +
                      "  }" +
                      "}",
              quoteIfNotNull(from != null ? from.toString() : null),
              page,
              pageSize,
              minutesAgo >= 0 ? minutesAgo : "null",
              quoteIfNotNull(to != null ? to.toString() : null),
              logQueryToGraphQLInput(logQuery));
  }
  
  private String quoteIfNotNull(Object value) {
      return value != null ? "\"" + value.toString() + "\"" : "null";
  }
  
  private String logQueryToGraphQLInput(LogQuery logQuery) {
    System.out.println("Service Name List: " + logQuery.getServiceName());
    System.out.println("Severity Text List: " + logQuery.getSeverityText());

    return "{"
            + " serviceName: " + convertListToGraphQLArray(logQuery.getServiceName()) + ","
            + " severityText: " + convertListToGraphQLArray(logQuery.getSeverityText())
            + "}";
}


private String convertListToGraphQLArray(List<String> list) {
  if (list == null || list.isEmpty()) {
      return "[]";
  }

  StringBuilder arrayBuilder = new StringBuilder("[");
  for (String item : list) {
      arrayBuilder.append("\"").append(item).append("\",");
  }
  arrayBuilder.deleteCharAt(arrayBuilder.length() - 1); // Remove the trailing comma
  arrayBuilder.append("]");

  return arrayBuilder.toString();
}
    



//LogSummaryChartCount

    @Query("LogSummaryChart")
    public List<LogMetrics> getLogMetricsCount(
            LocalDate startDate,
            LocalDate endDate,
            List<String> serviceNameList,
            Integer minutesAgo
    ) {
        return logQueryHandler.getLogMetricCount(serviceNameList, endDate, startDate, minutesAgo);
    }


//LogSortOrder

//  public LogDTO sortOrderTrace(
//             String sortOrder,
//             int page,
//             int pageSize,
//             LocalDate startDate,
//             LocalDate endDate,
//             int minutesAgo,
//             List<String> serviceNameList
            
//     ) {
//         if (page <= 0 || pageSize <= 0) {
//             return new LogDTO(Collections.emptyList(), 0);
//         }

//         try {
//             List<LogDTO> logs;

//             if ("new".equalsIgnoreCase(sortOrder)) {
//                 logs = logQueryHandler.getAllLogssOrderByCreatedTimeDesc(serviceNameList);
//             } else if ("old".equalsIgnoreCase(sortOrder)) {
//                 logs = logQueryHandler.getAllLogssAsc(serviceNameList);
//             } else if ("error".equalsIgnoreCase(sortOrder)) {
//                 logs = logQueryHandler.getErrorLogsByServiceNamesOrderBySeverityAndCreatedTimeDesc(serviceNameList);
//             } else {
//                 return new LogDTOQueryResult(Collections.emptyList(), 0);
//             }

//             // Rearrange 'from' and 'to' if necessary
//             if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
//                 LocalDate temp = startDate;
//                 startDate = endDate;
//                 endDate = temp;
//             }

//             // Convert LocalDate to Instant
//             Instant fromInstant = null;
//             Instant toInstant = null;

//             if (startDate != null && endDate != null) {
//                 // If both startDate and endDate are provided, consider the date range
//                 Instant startOfFrom = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
//                 Instant startOfTo = endDate.atStartOfDay(ZoneId.systemDefault()).toInstant();

//                 // Ensure that fromInstant is earlier than toInstant
//                 fromInstant = startOfFrom.isBefore(startOfTo) ? startOfFrom : startOfTo;
//                 toInstant = startOfFrom.isBefore(startOfTo) ? startOfTo : startOfFrom;

//                 toInstant = toInstant.plus(1, ChronoUnit.DAYS);
//             } else if (minutesAgo > 0) {
//                 // If minutesAgo is provided, calculate the time range based on minutesAgo
//                 Instant currentInstant = Instant.now();
//                 Instant minutesAgoInstant = currentInstant.minus(minutesAgo, ChronoUnit.MINUTES);

//                 // Calculate the start of the current day
//                 Instant startOfCurrentDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();

//                 // Ensure that fromInstant is later than the start of the current day
//                 if (minutesAgoInstant.isBefore(startOfCurrentDay)) {
//                     fromInstant = startOfCurrentDay;
//                 } else {
//                     fromInstant = minutesAgoInstant;
//                 }

//                 toInstant = currentInstant;
//             } else {
//                 // Handle the case when neither date range nor minutesAgo is provided
//                 return new LogDTOQueryResult(Collections.emptyList(), 0);
//             }

//             // Filter logs within the specified date range or based on minutes ago
//             Instant finalFromInstant = fromInstant;
//             Instant finalToInstant = toInstant;
//             logs = logs.stream()
//                     .filter(log -> isWithinDateRange(log.getCreatedTime(), finalFromInstant, finalToInstant))
//                     .collect(Collectors.toList());

//             int startIndex = (page - 1) * pageSize;
//             int endIndex = Math.min(startIndex + pageSize, logs.size());

//             if (startIndex >= endIndex || logs.isEmpty()) {
//                 return new LogDTOQueryResult(Collections.emptyList(), 0);
//             }

//             List<LogDTO> paginatedTraces = logs.subList(startIndex, endIndex);
//             int totalCount = logs.size();

//             return new LogDTOQueryResult(paginatedTraces, totalCount);
//         } catch (DateTimeParseException e) {
//             return new LogDTOQueryResult(Collections.emptyList(), 0);
//         }
//     }

//     private boolean isWithinDateRange(Date logTimestamp, Instant from, Instant to) {
//         Instant logInstant = logTimestamp.toInstant();

//         return (logInstant.equals(from) || logInstant.isAfter(from)) &&
//                 (logInstant.equals(to) || logInstant.isBefore(to));
//     }



@Query("LogSortOrder")
public List<LogDTO> sortOrderLog(
    String sortOrder,
    int page,
    int pageSize,
    LocalDate startDate,
    LocalDate endDate,
    int minutesAgo,
    List<String> serviceNameList
) {
if (page <= 0 || pageSize <= 0) {
    return Collections.emptyList();
}

try {
    List<LogDTO> logs;

    if ("new".equalsIgnoreCase(sortOrder)) {
        logs = logQueryHandler.getAllLogssOrderByCreatedTimeDesc(serviceNameList);
    } else if ("old".equalsIgnoreCase(sortOrder)) {
        logs = logQueryHandler.getAllLogssAsc(serviceNameList);
    } else if ("error".equalsIgnoreCase(sortOrder)) {
        logs = logQueryHandler.getErrorLogsByServiceNamesOrderBySeverityAndCreatedTimeDesc(serviceNameList);
    } else {
        return Collections.emptyList();
    }

    // Rearrange 'from' and 'to' if necessary
    if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
        LocalDate temp = startDate;
        startDate = endDate;
        endDate = temp;
    }

    // Convert LocalDate to Instant
    Instant fromInstant = null;
    Instant toInstant = null;

    if (startDate != null && endDate != null) {
        // If both startDate and endDate are provided, consider the date range
        Instant startOfFrom = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant startOfTo = endDate.atStartOfDay(ZoneId.systemDefault()).toInstant();

        // Ensure that fromInstant is earlier than toInstant
        fromInstant = startOfFrom.isBefore(startOfTo) ? startOfFrom : startOfTo;
        toInstant = startOfFrom.isBefore(startOfTo) ? startOfTo : startOfFrom;

        toInstant = toInstant.plus(1, ChronoUnit.DAYS);
    } else if (minutesAgo > 0) {
        // If minutesAgo is provided, calculate the time range based on minutesAgo
        Instant currentInstant = Instant.now();
        Instant minutesAgoInstant = currentInstant.minus(minutesAgo, ChronoUnit.MINUTES);

        // Calculate the start of the current day
        Instant startOfCurrentDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();

        // Ensure that fromInstant is later than the start of the current day
        if (minutesAgoInstant.isBefore(startOfCurrentDay)) {
            fromInstant = startOfCurrentDay;
        } else {
            fromInstant = minutesAgoInstant;
        }

        toInstant = currentInstant;
    } else {
        // Handle the case when neither date range nor minutesAgo is provided
        return Collections.emptyList();
    }

    // Filter logs within the specified date range or based on minutes ago
    Instant finalFromInstant = fromInstant;
    Instant finalToInstant = toInstant;
    logs = logs.stream()
            .filter(log -> isWithinDateRange(log.getCreatedTime(), finalFromInstant, finalToInstant))
            .collect(Collectors.toList());

    int startIndex = (page - 1) * pageSize;
    int endIndex = Math.min(startIndex + pageSize, logs.size());

    if (startIndex >= endIndex || logs.isEmpty()) {
        return Collections.emptyList();
    }

    List<LogDTO> paginatedTraces = logs.subList(startIndex, endIndex);
    return paginatedTraces;
} catch (DateTimeParseException e) {
    return Collections.emptyList();
}
}

// private boolean isWithinDateRange(Date logTimestamp, Instant from, Instant to) {
// Instant logInstant = logTimestamp.toInstant();

// return (logInstant.equals(from) || logInstant.isAfter(from)) &&
//         (logInstant.equals(to) || logInstant.isBefore(to));
// }



//LogSearchFunction



// public List<LogDTO> searchLogs(
//     int page,
//     int pageSize,
//     String keyword,
//     Instant from,
//     Instant to,
//     int minutesAgo) {

// try {
//     List<LogDTO> logList = logQueryHandler.searchLogs(keyword);

//     if (from != null && to != null) {
//         logList = filterLogsByDateRange(logList, from, to.plusSeconds(1));
//     } else if (minutesAgo > 0) {
//         Instant currentInstant = Instant.now();
//         Instant fromInstant = currentInstant.minus(minutesAgo, ChronoUnit.MINUTES);
//         Instant toInstant = currentInstant.minus(1, ChronoUnit.MINUTES);

//         logList = filterLogsByMinutesAgo(logList, fromInstant, toInstant);
//     }

//     int totalCount = logList.size();
//     int startIndex = (page - 1) * pageSize;
//     int endIndex = Math.min(startIndex + pageSize, totalCount);

//     if (startIndex >= endIndex || logList.isEmpty()) {
//         return Collections.emptyList();
//     }

//     return logList.subList(startIndex, endIndex);
// } catch (Exception e) {
//     // Handle exception
//     return Collections.emptyList();
// }
// }

// private List<LogDTO> filterLogsByDateRange(List<LogDTO> logs, Instant from, Instant to) {
// return logs.stream()
//         .filter(log -> isWithinDateRange(log.getCreatedTime(), from, to))
//         .collect(Collectors.toList());
// }

// private List<LogDTO> filterLogsByMinutesAgo(List<LogDTO> logs, Instant fromInstant, Instant toInstant) {
// // Calculate the start of the current day
// final Instant startOfCurrentDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();

// // Ensure that fromInstant is later than the start of the current day
// Instant adjustedFromInstant = fromInstant.isBefore(startOfCurrentDay) ? startOfCurrentDay : fromInstant;

// return logs.stream()
//         .filter(log -> isWithinDateRange(log.getCreatedTime(), adjustedFromInstant, toInstant))
//         .collect(Collectors.toList());
// }

private boolean isWithinDateRange(Date logTimestamp, Instant from, Instant to) {
Instant logInstant = logTimestamp.toInstant();
return (logInstant.equals(from) || logInstant.isAfter(from)) &&
        (logInstant.equals(to) || logInstant.isBefore(to));
}

}
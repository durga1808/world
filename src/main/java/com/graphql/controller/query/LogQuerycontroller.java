package com.graphql.controller.query;


import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphql.entity.queryentity.log.LogDTO;
import com.graphql.entity.queryentity.log.LogMetrics;
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



@Query("searchLogPaged")
public List<LogDTO> searchLogsPaged(
        LogQuery logQuery,
        int page,
        int pageSize,
        LocalDate from,
        LocalDate to,
        Integer minutesAgo
        // String sortOrder
) {
    List<LogDTO> logs = logQueryHandler.searchLogsPaged(logQuery, from, to, minutesAgo);

//     if ("new".equalsIgnoreCase(sortOrder)) {
//       logs = logQueryHandler.getFilterLogsByCreatedTimeDesc(logs);
//   } else if ("old".equalsIgnoreCase(sortOrder)) {
//       logs = logQueryHandler.getFilterLogssAsc(logs);
//   } else if ("error".equalsIgnoreCase(sortOrder)) {
//       logs = logQueryHandler.getFilterErrorLogs(logs);
//   }  else{
//     throw new IllegalArgumentException("Invalid sortOrder parameter. Use 'new', 'old', or 'error'.");
// }

    return logs;
}

  

@POST
    @Path("/filterLogs")
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
    
}
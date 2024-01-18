package com.graphql.controller.query;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.graphql.entity.queryentity.kepler.KeplerMetricDTO;
import com.graphql.entity.queryentity.kepler.Response.KeplerResponseData;
import com.graphql.handler.query.KeplerMetricHandler;
import com.graphql.repo.query.KeplerMetricRepo;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@GraphQLApi

@Path("/kepler")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class KeplerQueryController {
    
  @Inject
  KeplerMetricHandler keplerMetricHandler;

  @Inject
  KeplerMetricRepo keplerMetricRepo;


 @Query("getAllKeplerData")
   public List<KeplerMetricDTO> getAllKeplerData(){
       return keplerMetricHandler.getAllKepler();
   }



 @Query
  public List<KeplerResponseData> getAllKeplerMetricDatas(
   @Name("from") LocalDate from,
    @Name("to") LocalDate to,
   @Name("minutesAgo") int minutesAgo,
    @Name("type") String type,
    @Name("keplerType") List<String> keplerTypeList,
    @Name("page") int page,
    @Name("pageSize") int pageSize
) throws JsonProcessingException {
    LocalDateTime APICallStart = LocalDateTime.now();

    List<KeplerResponseData> keplerMetricData = keplerMetricHandler.getAllKeplerByDateAndTime(
            from,
            to,
            minutesAgo,
            type,
            keplerTypeList,
            page,
            pageSize
    );

    try {
        LocalDateTime APICallEnd = LocalDateTime.now();
        Duration duration = Duration.between(APICallStart, APICallEnd);

        return keplerMetricData;
    } catch (Exception e) {
        // Handle exception if needed
        return Collections.emptyList(); // or throw an exception
    }
}

}

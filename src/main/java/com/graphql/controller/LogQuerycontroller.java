package com.graphql.controller;

import java.util.List;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

import com.graphql.entity.otellog.OtelLog;
import com.graphql.handler.LogQueryHandler;
import com.graphql.repo.LogQueryRepo;

import jakarta.inject.Inject;
@GraphQLApi
public class LogQuerycontroller{
    
 @Inject
 LogQueryHandler logQueryHandler;
 
 @Inject 
 LogQueryRepo logQueryRepo;




   @Query("getAllLogsData")
   public List<OtelLog> getAllLogData(){
       return logQueryHandler.getAllLogs();
   }



//    @Mutation
//    public LogDTO createProduct(LogDTO logDTO){
//     LogDTO log =new LogDTO();
//      logQueryHandler.addProductDetails(logDTO);
//         return log;
//    }
}


// @Mutation
//     public ProductDetails createProductDetails(
//             @Name("firstname") String firstname,
//             @Name("lastname") String lastname,
//             @Name("address") List<Address> address,
//             @Name("productCategories") List<ProductCategory> productCategories
//     ) {
//         ProductDetails productDetails = new ProductDetails(firstname, lastname, address, productCategories);
//         productDetailsService.addProductDetails(productDetails);
//         return productDetails;
//     }
  






   //  @Query("getAllProductDetails")
   //  public List<ProductDetails> getAllProductDetails() {
   //      return productDetailsService.getAllProductDetails();
   //  }

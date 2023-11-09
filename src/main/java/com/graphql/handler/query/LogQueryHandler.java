package com.graphql.handler.query;



import java.util.List;

import com.graphql.entity.otellog.OtelLog;
import com.graphql.repo.query.LogQueryRepo;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;


@ApplicationScoped
public class LogQueryHandler {
     @Inject
     LogQueryRepo logQueryRepo;


    public List<OtelLog> getAllLogs(){
        List<OtelLog> logs =logQueryRepo.listAll();
        return logs;
    }
   









    // public List<ProductDetails> getAllProductDetails() {
    //     List<ProductDetails> allProducts = productRepo.listAll();
    //     return allProducts;
    // }
}

package com.graphql.handler;



import java.util.List;

import com.graphql.entity.queryentity.log.LogDTO;
import com.graphql.repo.LogQueryRepo;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;


@ApplicationScoped
public class LogQueryHandler {
     @Inject
     LogQueryRepo logQueryRepo;


    public List<LogDTO> getAllLogs(){
        List<LogDTO> logs =logQueryRepo.listAll();
        return logs;
    }
   









    // public List<ProductDetails> getAllProductDetails() {
    //     List<ProductDetails> allProducts = productRepo.listAll();
    //     return allProducts;
    // }
}

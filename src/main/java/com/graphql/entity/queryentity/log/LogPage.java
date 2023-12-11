package com.graphql.entity.queryentity.log;

import java.util.List;

import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@MongoEntity(collection = "LogDTO", database = "OtelLog")
public class LogPage {
     private List<LogDTO> logs;
    private int totalCount;

}

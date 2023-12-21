package com.graphql.entity.queryentity.trace;

import java.util.List;

import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@MongoEntity(collection="TraceDTO",database="OtelTrace")
public class TracePage {
     private List<TraceDTO> traces;
    private int totalCount;
}

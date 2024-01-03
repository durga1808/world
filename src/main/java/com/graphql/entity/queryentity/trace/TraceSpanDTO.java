package com.graphql.entity.queryentity.trace;

import java.util.Date;
import java.util.List;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TraceSpanDTO extends PanacheMongoEntity{
     private String traceId;
    private String serviceName;
    private String methodName;
    private String operationName;
    private Long duration;
    private Long statusCode;
    private String spanCount;
    private Date createdTime;
    private List<SpanDTO> spanDTOs;
}
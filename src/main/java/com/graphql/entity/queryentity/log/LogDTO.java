package com.graphql.entity.queryentity.log;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import com.graphql.entity.otellog.ScopeLogs;


import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@MongoEntity(collection = "LogDTO", database = "OtelLog")
public class LogDTO {
    private String serviceName;
    private String traceId;
    private String spanId;
    private Date createdTime;
    private String severityText;
    private List<ScopeLogs> scopeLogs;
  
  }

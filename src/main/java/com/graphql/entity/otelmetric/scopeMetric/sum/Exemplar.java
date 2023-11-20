package com.graphql.entity.otelmetric.scopeMetric.sum;

import org.eclipse.microprofile.graphql.Name;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Name("SumExemplar")

public class Exemplar {
    private String timeUnixNano;
    private double asDouble;
    private Integer asInt;
    private String spanId;
    private String traceId;
    
}
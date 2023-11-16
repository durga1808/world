package com.graphql.entity.otelmetric.scopeMetric.histogram;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HistogramDataPointAttributeValue {
    private String stringValue;
    private Integer intValue;
}

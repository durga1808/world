package com.graphql.entity.otelmetric.scopeMetric.histogram;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HistogramDataPointAttribute {
    private String key;
    private HistogramDataPointAttributeValue value;
}

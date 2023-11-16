package com.graphql.entity.otelmetric.scopeMetric.gauge;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GaugeDataPointAttribute {
    private String key;
    private GaugeDataPointAttributeValue value;

}

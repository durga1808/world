package com.graphql.entity.otelmetric.scopeMetric;

import java.util.List;

import com.graphql.entity.otelmetric.scopeMetric.sum.SumDataPoint;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MetricSum {
    private List<SumDataPoint> dataPoints;
    private int aggregationTemporality;
    private Boolean isMonotonic;
}

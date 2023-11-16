package com.graphql.entity.otelmetric.scopeMetric;

import java.util.List;

import com.graphql.entity.otelmetric.scopeMetric.gauge.GaugeDataPoint;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MetricGauge {
    private List<GaugeDataPoint> dataPoints;
    private int aggregationTemporality;
}

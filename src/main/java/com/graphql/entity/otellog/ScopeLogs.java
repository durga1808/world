package com.graphql.entity.otellog;

import java.util.List;

import com.graphql.entity.otellog.scopeLogs.LogRecord;
import com.graphql.entity.otellog.scopeLogs.Scope;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScopeLogs {
    private Scope scope;
    private List<LogRecord> logRecords;
}

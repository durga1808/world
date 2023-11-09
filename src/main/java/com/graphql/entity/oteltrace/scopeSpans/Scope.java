package com.graphql.entity.oteltrace.scopeSpans;

import org.eclipse.microprofile.graphql.Name;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Name("ScopeTraceInput")
public class Scope {
    private String name;
    private String version;
}

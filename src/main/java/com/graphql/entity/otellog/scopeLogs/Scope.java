package com.graphql.entity.otellog.scopeLogs;


import org.eclipse.microprofile.graphql.Name;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Name("ScopeLogInput")
public class Scope {
    private String name;
}

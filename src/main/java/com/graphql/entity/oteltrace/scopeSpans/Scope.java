package com.graphql.entity.oteltrace.scopeSpans;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Scope {
    private String name;
    private String version;


}

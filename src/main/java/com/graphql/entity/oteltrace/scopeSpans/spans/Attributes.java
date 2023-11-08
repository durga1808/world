package com.graphql.entity.oteltrace.scopeSpans.spans;

import com.graphql.entity.oteltrace.resource.attributes.Value;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Attributes {
    private String key;
    private Value value;
   
}
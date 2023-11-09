package com.graphql.entity.oteltrace.scopeSpans.spans;

import com.graphql.entity.oteltrace.scopeSpans.spans.attributes.Value;

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
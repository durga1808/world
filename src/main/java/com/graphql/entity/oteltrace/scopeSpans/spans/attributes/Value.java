package com.graphql.entity.oteltrace.scopeSpans.spans.attributes;

import org.eclipse.microprofile.graphql.Name;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
//@Name("ScopeSpansAttributeValue")
public class Value {
    private String intValue;
    private String stringValue;
}
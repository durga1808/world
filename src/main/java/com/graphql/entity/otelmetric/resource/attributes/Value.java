package com.graphql.entity.otelmetric.resource.attributes;


import org.eclipse.microprofile.graphql.Name;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Name("ResourceValue")
public class Value {
     private String stringValue;
    private int intValue;
    private ArrayValue arrayValue;
}

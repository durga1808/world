package com.graphql.entity.oteltrace.resource;

import com.graphql.entity.oteltrace.resource.attributes.Value;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Attribute {
    private String key;
    private Value value;
}
package com.graphql.entity.oteltrace;

import java.util.List;

import com.graphql.entity.oteltrace.resource.Attribute;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Resource {
    private List<Attribute> attributes;
}

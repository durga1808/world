package com.graphql.entity.otelmetric;

import java.util.List;

import com.graphql.entity.otelmetric.resource.Attribute;

import lombok.NoArgsConstructor;

import lombok.AllArgsConstructor;

import lombok.Data;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class Resource {
    private List<Attribute> attributes; 
}

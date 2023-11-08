package com.graphql.entity.otellog;

import java.util.List;

import com.graphql.entity.otellog.resource.Attribute;

import lombok.NoArgsConstructor;

import lombok.AllArgsConstructor;

import lombok.Data;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class Resource {
    private List<Attribute> attributes;
}

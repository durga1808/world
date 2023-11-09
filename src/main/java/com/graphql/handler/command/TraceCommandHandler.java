package com.graphql.handler.command;

import com.graphql.repo.command.TraceCommandRepo;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class TraceCommandHandler {
    @Inject
    TraceCommandRepo traceCommandRepo;
}

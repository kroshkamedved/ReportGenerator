package com.en.reportgenerator.domain;


public record Experiment(int id, int stageId, int ownerId, String ownerName, String comment,
                         String svg) implements AllFieldsToStringReady {
}

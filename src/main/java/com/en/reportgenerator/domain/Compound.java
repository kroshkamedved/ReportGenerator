package com.en.reportgenerator.domain;

public record Compound(int id, String name, float equivalent, float mw, double amount,
                       float moles, String formula, String condition, String synonym, String cas,
                       @SVGColumn String structure) implements AllFieldsToStringReady {
}

package com.en.reportgenerator.domain;

public record Compound(int id, String name, float equality, float mw, double amount,
                       float moles, String formula, String condition, String synonym,
                       @SVGColumn String structure) implements AllFieldsToStringReady {
}

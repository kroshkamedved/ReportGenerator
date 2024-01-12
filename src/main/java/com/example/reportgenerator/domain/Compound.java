package com.example.reportgenerator.domain;

public record Compound(int id, String name, float equality, float mw, double amount,
                       float moles, String formula, String condition, @SVGColumn String svg) implements AllFieldsToStringReady {
}

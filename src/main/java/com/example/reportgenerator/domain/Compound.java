package com.example.reportgenerator.domain;

public record Compound(int id, int experimentId, String name, float equality, String mf, float mw, double amount,
                       float moles, String formula, String condition, @SVGColumn String svg) implements AllFieldsToStringReady {
}

package com.example.reportgenerator.mock;

import com.example.reportgenerator.domain.AllFieldsToStringReady;

public record User(int id, String name, String password, long salary, int random) implements AllFieldsToStringReady {
}
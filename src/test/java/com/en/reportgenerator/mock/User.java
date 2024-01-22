package com.en.reportgenerator.mock;

import com.en.reportgenerator.domain.AllFieldsToStringReady;

public record User(int id, String name, String password, long salary, int random) implements AllFieldsToStringReady {
}
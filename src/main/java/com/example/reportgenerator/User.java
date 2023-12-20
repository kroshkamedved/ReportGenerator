package com.example.reportgenerator;

public record User(int id,String name, String password, long salary) implements AllFieldsToStringReady {}
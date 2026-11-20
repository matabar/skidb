package com.matmon.skidb.exception;

public class SchemaViolationException extends DatabaseException {

    public SchemaViolationException(String message) {
        super(message);
    }
}

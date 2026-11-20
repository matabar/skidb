package com.matmon.skidb.exception;

public class DuplicateKeyException extends DatabaseException {

    public DuplicateKeyException(String message) {
        super(message);
    }
}

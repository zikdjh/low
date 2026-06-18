package com.back.exception;

public class SqlInsertException extends RuntimeException {
    public SqlInsertException(String message) {
        super(message);
    }
}

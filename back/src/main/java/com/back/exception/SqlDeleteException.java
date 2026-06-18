package com.back.exception;

public class SqlDeleteException extends RuntimeException {
    public SqlDeleteException(String message) {
        super(message);
    }
}

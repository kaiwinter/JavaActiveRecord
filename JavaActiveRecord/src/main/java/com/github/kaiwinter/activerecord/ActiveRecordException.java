package com.github.kaiwinter.activerecord;

public final class ActiveRecordException extends Exception {

    private static final long serialVersionUID = 8624965217032029023L;

    public ActiveRecordException(String message, Throwable throwable) {
        super(message, throwable);
    }
}

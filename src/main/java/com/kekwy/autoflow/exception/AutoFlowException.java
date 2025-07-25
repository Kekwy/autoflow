package com.kekwy.autoflow.exception;

public class AutoFlowException extends RuntimeException {
    public AutoFlowException(String message) {
        super(message);
    }

    public AutoFlowException(Exception e) {
        super(e);
    }
}

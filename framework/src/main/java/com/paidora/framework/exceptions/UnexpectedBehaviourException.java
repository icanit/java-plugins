package com.paidora.framework.exceptions;

/** Просто исключение для внутреннего, локального использования */
public class UnexpectedBehaviourException extends Exception {
    public UnexpectedBehaviourException() {
    }

    public UnexpectedBehaviourException(String message) {
        super(message);
    }

    public UnexpectedBehaviourException(Throwable cause) {
        super(cause);
    }

    public UnexpectedBehaviourException(String message, Throwable cause) {
        super(message, cause);
    }
}

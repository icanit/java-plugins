package com.paidora.framework.modules.exceptions;

public class ModuleInstanceException extends Exception {
    public ModuleInstanceException(String message) {
        super(message);
    }

    public ModuleInstanceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ModuleInstanceException(Throwable cause) {
        super(cause);
    }

    protected ModuleInstanceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

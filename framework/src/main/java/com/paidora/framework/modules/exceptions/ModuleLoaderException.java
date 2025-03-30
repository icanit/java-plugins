package com.paidora.framework.modules.exceptions;

public class ModuleLoaderException extends Exception {
    public ModuleLoaderException(String message) {
        super(message);
    }

    public ModuleLoaderException(String message, Throwable cause) {
        super(message, cause);
    }

    public ModuleLoaderException(Throwable cause) {
        super(cause);
    }

    protected ModuleLoaderException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

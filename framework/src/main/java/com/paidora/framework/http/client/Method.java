package com.paidora.framework.http.client;

/**
 *
 */
public enum Method {
    GET(false),
    POST(true),
    OPTIONS(false),
    HEAD(false),
    PUT(true),
    PATCH(true),
    DELETE(false),
    TRACE(false),
    CONNECT(false),
    PROPFIND(false),
    TRACK(false);

    private final boolean allowsBody;

    Method(boolean allowsBody) {
        this.allowsBody = allowsBody;
    }

    public boolean allowsBody() {
        return allowsBody;
    }
}

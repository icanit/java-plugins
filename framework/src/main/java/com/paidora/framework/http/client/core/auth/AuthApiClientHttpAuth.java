package com.paidora.framework.http.client.core.auth;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "of")
public class AuthApiClientHttpAuth implements IApiClientHttpAuth {

    private final String token;

    @Override
    public String getHeaderName() {
        return "Auth";
    }

    @Override
    public String getHeaderValue() {
        return token;
    }
}

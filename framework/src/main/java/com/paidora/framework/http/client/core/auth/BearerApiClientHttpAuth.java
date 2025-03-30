package com.paidora.framework.http.client.core.auth;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "of")
public class BearerApiClientHttpAuth implements IApiClientHttpAuth {

    private final String token;

    @Override
    public String getHeaderName() {
        return "Authorization";
    }

    @Override
    public String getHeaderValue() {
        return "Bearer " + token;
    }
}

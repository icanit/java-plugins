package com.paidora.framework.http.client.core.auth;

import com.paidora.framework.http.client.BasicAuthInfo;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "of")
public class BasicApiClientHttpAuth implements IApiClientHttpAuth {
    private final String login;
    private final String password;

    @Override
    public String getHeaderName() {
        return "Authorization";
    }

    @Override
    public String getHeaderValue() {
        return new BasicAuthInfo(login, password).getCompiled();
    }
}

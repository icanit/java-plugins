package com.paidora.framework.http.client.core.auth;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "of")
public class TokenApiClientHttpAuth implements IApiClientHttpAuth {

        private final String token;

        @Override
        public String getHeaderName() {
            return "Authorization";
        }

        @Override
        public String getHeaderValue() {
            return "Token " + token;
        }
    }

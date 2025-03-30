package com.paidora.framework.http.client.core;

import lombok.Data;

@Data
public class ApiClientSSLBase64Params {
    private final String cert;
    private final String password;
}

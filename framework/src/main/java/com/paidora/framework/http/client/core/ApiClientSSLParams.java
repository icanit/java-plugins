package com.paidora.framework.http.client.core;

import lombok.Data;

@Data
public class ApiClientSSLParams {
    private final String privateKeyPem;
    private final String certificatePem;
}

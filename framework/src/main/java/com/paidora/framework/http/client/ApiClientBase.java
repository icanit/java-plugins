package com.paidora.framework.http.client;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.paidora.framework.exceptions.UnexpectedBehaviourException;
import com.paidora.framework.http.client.core.ApiClientBodyType;
import com.paidora.framework.http.client.core.ApiClientHttpRequest;
import com.paidora.framework.ssl.SSLUtil;
import com.paidora.framework.utils.uri.Uri;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.DefaultRedirectStrategy;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
@Slf4j
public abstract class ApiClientBase {

    protected static final String API_ERROR = "api_error";
    private final Long responseTimeout;
    private final String url;
    private final PoolingHttpClientConnectionManager connManager;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final XmlMapper xmlMapper = new XmlMapper();
    private boolean isBase64SSLContext;

    public ApiClientBase(String url, Long responseTimeoutSeconds) {
        this(url, null, responseTimeoutSeconds);
    }

    public ApiClientBase(String url, PoolingHttpClientConnectionManager connManager, Long responseTimeoutSeconds) {
        this.url = url;
        this.responseTimeout = responseTimeoutSeconds;
        this.connManager = connManager;
    }

    public <TRequest, TResponse> TResponse makeApiRequest(ApiClientHttpRequest<TRequest, TResponse> request) throws UnexpectedBehaviourException {
        SSLContext sslContext;

        if (request.getApiClientSSLBase64Params() != null) {
            try {
                sslContext = SSLUtil.getSSLContextForBase64Certificate(request.getApiClientSSLBase64Params().getPassword(), request.getApiClientSSLBase64Params().getCert());
            } catch (Exception e) {
                log.info("Api client ssl context error", e);
                throw new UnexpectedBehaviourException("ssl_context");
            }
        } else {
            try {
                sslContext = request.getApiClientSSLParams() != null ? SSLUtil.getSSLContextForCertificate(request.getApiClientSSLParams().getPrivateKeyPem(), request.getApiClientSSLParams().getCertificatePem()) : null;
            } catch (Exception e) {
                log.info("Api client ssl context error", e);
                throw new UnexpectedBehaviourException("ssl_context");
            }
        }
        CloseableHttpClient httpClient;
        try {
            httpClient = getHttpClient(responseTimeout, sslContext, request.isFollowHttpRedirect());

            String requestBody = null;
            ContentType contentType = null;
            ClassicHttpRequest httpRequest;

            if (request.getMethod().allowsBody()) {
                if (request.getRequest() != null) {
                    switch (request.getRequestBodyType()) {
                        case JSON:
                            requestBody = objectMapper.writeValueAsString(request.getRequest());
                            contentType = ContentType.create(ContentType.APPLICATION_JSON.getMimeType(), request.getRequestBodyCharset());
                            break;
                        case XML:
                            requestBody = xmlMapper.writeValueAsString(request.getRequest());
                            contentType = ContentType.create(ContentType.APPLICATION_XML.getMimeType(), request.getRequestBodyCharset());
                            break;
                        case FORM:
                            @SuppressWarnings("unchecked")
                            Map<String, String> map = (Map<String, String>) request.getRequest();
                            requestBody = Uri.compileQuery(map);
                            contentType = ContentType.create(ContentType.APPLICATION_FORM_URLENCODED.getMimeType(), request.getRequestBodyCharset());
                            break;
                        case TEXT:
                            requestBody = request.getRequest().toString();
                            contentType = ContentType.create(ContentType.TEXT_PLAIN.getMimeType(), request.getRequestBodyCharset());
                            break;
                        case TEXT_XML:
                            requestBody = request.getRequest().toString();
                            contentType = ContentType.create(ContentType.TEXT_XML.getMimeType(), request.getRequestBodyCharset());
                            break;
                        default:
                            throw new UnexpectedBehaviourException("Unexpected RequestBodyType value: " + request.getRequestBodyType());
                    }
                }

                ClassicHttpRequest enclosingRequestBase;
                switch (request.getMethod()) {
                    case POST:
                        enclosingRequestBase = new HttpPost(request.getUrl());
                        break;
                    case PUT:
                        enclosingRequestBase = new HttpPut(request.getUrl());
                        break;
                    case PATCH:
                        enclosingRequestBase = new HttpPatch(request.getUrl());
                        break;
                    default:
                        throw new UnexpectedBehaviourException("Unexpected method value: " + request.getMethod());
                }
                if (requestBody != null) {
                    if (request.getRequestPreprocessor() != null) {
                        requestBody = request.getRequestPreprocessor().preprocessHttpRequest(enclosingRequestBase, request, requestBody);
                    }
                    enclosingRequestBase.setEntity(new StringEntity(requestBody, contentType));
                }
                httpRequest = enclosingRequestBase;

            } else {
                switch (request.getMethod()) {
                    case GET:
                        httpRequest = new HttpGet(request.getUrl());
                        break;
                    case DELETE:
                        httpRequest = new HttpDelete(request.getUrl());
                        break;
                    default:
                        throw new UnexpectedBehaviourException("Unexpected method value: " + request.getMethod());
                }
            }

            if (request.getApiClientHttpAuth() != null) {
                httpRequest.addHeader(request.getApiClientHttpAuth().getHeaderName(), request.getApiClientHttpAuth().getHeaderValue());
            }

            if (!request.getHeaders().entrySet().isEmpty()) {
                for (Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
                    httpRequest.addHeader(entry.getKey(), entry.getValue());
                }
            }

            {//возможно этот блок лишний ибо мы никогда не заполняли Accept заголовок, но пусть будет
                switch (request.getResponseBodyType()) {
                    case JSON:
                        httpRequest.addHeader(HttpHeaders.ACCEPT, "application/json");
                        break;
                    case XML:
                        httpRequest.addHeader(HttpHeaders.ACCEPT, "application/xml");
                        break;
                    case TEXT:
                        httpRequest.addHeader(HttpHeaders.ACCEPT, "*/*");
                        break;
                    default:
                        break;
                }
            }

            var requestHeadersMap = new HashMap<String, String>();
            for (var hdr : httpRequest.getHeaders()) {
                requestHeadersMap.put(hdr.getName(), hdr.getValue());
            }


            long startTime = System.currentTimeMillis();
            try (var httpResponse = httpClient.execute(httpRequest)) {
                log.info("Received response: " + httpResponse);
                var statusCode = httpResponse.getCode();

                var responseHeaders = new HashMap<String, String>();
                for (var hdr : httpResponse.getHeaders()) {
                    responseHeaders.put(hdr.getName(), hdr.getValue());
                }

                var entity = httpResponse.getEntity();
                String bodyString = null;
                byte[] bodyBytes = null;
                if (request.getResponseBodyType() != ApiClientBodyType.BLOB) {
                    bodyString = entity != null ? EntityUtils.toString(httpResponse.getEntity(), request.getResponseBodyCharset()) : null;
                    if (bodyString != null) {
                        log.info("Response body: " + bodyString);
                        if (request.getResponseBodyPreprocessor() != null) {
                            bodyString = request.getResponseBodyPreprocessor().preprocessHttpResponse(httpResponse, bodyString);
                        }
                    }
                } else {
                    bodyBytes = entity != null ? EntityUtils.toByteArray(httpResponse.getEntity()) : null;
                    if (bodyBytes != null) {
                        log.info("Response body bytes with length: " + bodyBytes.length);
                    }
                }

                var responseCodeProcessors = request.getHttpResponseCodeProcessors().get(httpResponse.getCode());
                if (responseCodeProcessors != null) {
                    return responseCodeProcessors.processResponse(httpResponse, bodyString);
                }
                if (
                        (request.getResponseBodyType() != null && request.getResponseBodyType() != ApiClientBodyType.BLOB && bodyString == null)
                                || (request.getResponseBodyType() == ApiClientBodyType.BLOB && bodyBytes == null)
                ) {
                    throw new UnexpectedBehaviourException("response is empty");
                }
                if (statusCode == 200 || statusCode == 201) {
                    switch (request.getResponseBodyType()) {
                        case JSON:
                            return objectMapper.readerFor(request.getResponseClassType()).with(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT).readValue(bodyString);
                        case XML:
                            return xmlMapper.readValue(bodyString, request.getResponseClassType());
                        case TEXT:
                            @SuppressWarnings("unchecked") var response = (TResponse) bodyString;
                            return response;
                        case BLOB:
                            @SuppressWarnings("unchecked") var byteResponse = (TResponse) bodyBytes;
                            return byteResponse;
                        default:
                            throw new UnexpectedBehaviourException("Unexpected ResponseBodyType value: " + request.getRequestBodyType());
                    }
                } else {
                    log.info("Invalid status code from server: " + statusCode);
                    throw new UnexpectedBehaviourException("http:" + statusCode);
                }
            } catch (UnexpectedBehaviourException e) {
                throw e;
            } catch (Exception e) {
                log.info("Error sending request", e);
                throw new UnexpectedBehaviourException(e.getMessage());
            } finally {
                if (connManager == null) {
                    httpClient.close();
                }
            }
        } catch (IOException e) {
            log.info("Error sending request", e);
            throw new UnexpectedBehaviourException(e.getMessage());
        }
    }

    protected CloseableHttpClient getHttpClient() throws UnexpectedBehaviourException {
        return getHttpClient(this.responseTimeout, null, false);
    }

    protected CloseableHttpClient getHttpClientWithResponseTimeout(Long responseTimeout) throws UnexpectedBehaviourException {
        return getHttpClient(responseTimeout, null, false);
    }

    protected CloseableHttpClient getHttpClientWithResponseTimeoutWithEnabledRedirect(Long responseTimeout) throws UnexpectedBehaviourException {
        return getHttpClient(responseTimeout, null, true);
    }

    protected CloseableHttpClient getHttpClientWithSSLContext(Long responseTimeout, SSLContext sslContext) throws UnexpectedBehaviourException {
        return getHttpClient(responseTimeout, sslContext, false);
    }

    protected CloseableHttpClient getHttpClientWithSSLContextWithEnableRedirect(Long responseTimeout, SSLContext sslContext) throws UnexpectedBehaviourException {
        return getHttpClient(responseTimeout, sslContext, true);
    }

    protected CloseableHttpClient getHttpClient(Long timeout, SSLContext sslContext, boolean enableRedirect) throws UnexpectedBehaviourException {
        try {
            int connectTimeout = (timeout == null ? this.responseTimeout.intValue() : timeout.intValue()) * 1000;
            var configBuilder = RequestConfig.custom()
                    .setConnectionRequestTimeout(Timeout.of(85, TimeUnit.SECONDS))
                    .setResponseTimeout(Timeout.of(85, TimeUnit.SECONDS))
                    .setCookieSpec("ignoreCookies");
            if (enableRedirect) {
                configBuilder.setRedirectsEnabled(true)
                        .setMaxRedirects(2)
                        .setCircularRedirectsAllowed(false);
            } else {
                configBuilder.setRedirectsEnabled(false);
            }

            var requestConfig = configBuilder.build();
            var clientBuilder = HttpClients.custom()
                    .setDefaultRequestConfig(requestConfig);
            SSLConnectionSocketFactory sslSf;
            if (sslContext != null) {
                sslSf = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
            } else {
                var builder = new SSLContextBuilder();
                builder.loadTrustMaterial(null, new TrustAllStrategy());
                sslSf = new SSLConnectionSocketFactory(builder.build(), NoopHostnameVerifier.INSTANCE);
            }

            var socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("https", sslSf)
                    .register("http", new PlainConnectionSocketFactory())
                    .build();
            var cm = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
            cm.setDefaultSocketConfig(SocketConfig.custom()
                    .setSoTimeout(Timeout.of(85, TimeUnit.SECONDS))
                    .build());
            cm.setDefaultConnectionConfig(ConnectionConfig.custom()
                    .setSocketTimeout(Timeout.of(85, TimeUnit.SECONDS))
                    .setConnectTimeout(Timeout.of(85, TimeUnit.SECONDS))
                    .setTimeToLive(TimeValue.ofMinutes(10))
                    .build());
            cm.setMaxTotal(100);
            cm.setDefaultMaxPerRoute(100);

            clientBuilder.setConnectionManager(cm);
            if (enableRedirect) {
                clientBuilder.setRedirectStrategy(new DefaultRedirectStrategy());
            }
            if (connManager != null) {
                clientBuilder.setConnectionManager(connManager);
            }
            return clientBuilder.build();
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException ex) {
            throw new UnexpectedBehaviourException("http:client:error", ex);
        }
    }
}

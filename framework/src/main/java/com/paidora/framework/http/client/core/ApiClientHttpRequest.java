package com.paidora.framework.http.client.core;

import com.paidora.framework.http.client.core.auth.IApiClientHttpAuth;
import com.paidora.framework.http.client.Method;
import lombok.Getter;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * defaults:
 * requestBodyCharset:  StandardCharsets.UTF_8
 *
 * @param <TRequest>
 * @param <TResponse>
 */
@Getter
public class ApiClientHttpRequest<TRequest, TResponse> {
    private final Map<Integer, IApiClientHttpResponseCodeProcessor<TResponse>> httpResponseCodeProcessors = new HashMap<>();
    private final Map<String, String> headers = new HashMap<>();
    private String url;
    private Method method;
    private ApiClientBodyType requestBodyType;
    private Object request;
    private Charset requestBodyCharset = StandardCharsets.UTF_8;
    private Charset responseBodyCharset = StandardCharsets.UTF_8;
    private IApiRequestPreprocessor<TRequest> requestPreprocessor;
    private IApiResponseStringBodyPreprocessor responseBodyPreprocessor;
    private IApiClientHttpAuth apiClientHttpAuth;
    private ApiClientBodyType responseBodyType;
    private ApiClientSSLParams apiClientSSLParams;
    private ApiClientSSLBase64Params apiClientSSLBase64Params;
    private boolean followHttpRedirect;
    private Class<TResponse> responseClassType;

    //region factory methods
    public static <TRequest, TResponse> ApiClientHttpRequest<TRequest, TResponse> get(String url,
                                                                                      ApiClientBodyType responseBodyType,
                                                                                      Class<TResponse> responseClassType) {
        var api = new ApiClientHttpRequest<TRequest, TResponse>();
        api.url = url;
        api.method = Method.GET;
        api.responseBodyType = responseBodyType;
        api.responseClassType = responseClassType;
        return api;
    }

    public static <TRequest, TResponse> ApiClientHttpRequest<TRequest, TResponse> post(String url,
                                                                                       ApiClientBodyType requestBodyType,
                                                                                       TRequest request,
                                                                                       ApiClientBodyType responseBodyType,
                                                                                       Class<TResponse> responseClassType) {
        var api = new ApiClientHttpRequest<TRequest, TResponse>();
        api.url = url;
        api.method = Method.POST;
        api.requestBodyType = requestBodyType;
        api.request = request;
        api.responseBodyType = responseBodyType;
        api.responseClassType = responseClassType;
        return api;
    }

    public static <TRequest, TResponse> ApiClientHttpRequest<TRequest, TResponse> put(String url,
                                                                                      ApiClientBodyType requestBodyType,
                                                                                      TRequest request,
                                                                                      ApiClientBodyType responseBodyType,
                                                                                      Class<TResponse> responseClassType) {
        var api = new ApiClientHttpRequest<TRequest, TResponse>();
        api.url = url;
        api.method = Method.PUT;
        api.requestBodyType = requestBodyType;
        api.request = request;
        api.responseBodyType = responseBodyType;
        api.responseClassType = responseClassType;
        return api;
    }

    public static <TRequest, TResponse> ApiClientHttpRequest<TRequest, TResponse> postXml(String url,
                                                                                          TRequest request,
                                                                                          ApiClientBodyType responseBodyType,
                                                                                          Class<TResponse> responseClassType) {
        var api = new ApiClientHttpRequest<TRequest, TResponse>();
        api.url = url;
        api.method = Method.POST;
        api.requestBodyType = ApiClientBodyType.XML;
        api.request = request;
        api.responseBodyType = responseBodyType;
        api.responseClassType = responseClassType;
        return api;
    }

    public static <TRequest, TResponse> ApiClientHttpRequest<TRequest, TResponse> postJson(String url,
                                                                                           TRequest request,
                                                                                           ApiClientBodyType responseBodyType,
                                                                                           Class<TResponse> responseClassType) {
        var api = new ApiClientHttpRequest<TRequest, TResponse>();
        api.url = url;
        api.method = Method.POST;
        api.requestBodyType = ApiClientBodyType.JSON;
        api.request = request;
        api.responseBodyType = responseBodyType;
        api.responseClassType = responseClassType;
        return api;
    }

    public static <TRequest, TResponse> ApiClientHttpRequest<TRequest, TResponse> postForm(String url,
                                                                                           Map<String, String> request,
                                                                                           ApiClientBodyType responseBodyType,
                                                                                           Class<TResponse> responseClassType) {
        var api = new ApiClientHttpRequest<TRequest, TResponse>();
        api.url = url;
        api.method = Method.POST;
        api.requestBodyType = ApiClientBodyType.FORM;
        api.request = request;
        api.responseBodyType = responseBodyType;
        api.responseClassType = responseClassType;
        return api;
    }

    public ApiClientHttpRequest<TRequest, TResponse> setRequestBodyCharset(Charset requestBodyCharset) {
        this.requestBodyCharset = requestBodyCharset;
        return this;
    }

    public ApiClientHttpRequest<TRequest, TResponse> setRequestPreprocessor(IApiRequestPreprocessor<TRequest> requestPreprocessor) {
        this.requestPreprocessor = requestPreprocessor;
        return this;
    }

    public ApiClientHttpRequest<TRequest, TResponse> setResponseBodyPreprocessor(IApiResponseStringBodyPreprocessor responseBodyPreprocessor) {
        this.responseBodyPreprocessor = responseBodyPreprocessor;
        return this;
    }

    public ApiClientHttpRequest<TRequest, TResponse> setApiClientHttpAuth(IApiClientHttpAuth apiClientHttpAuth) {
        this.apiClientHttpAuth = apiClientHttpAuth;
        return this;
    }

    public ApiClientHttpRequest<TRequest, TResponse> setApiClientSSLParams(ApiClientSSLParams apiClientSSLParams) {
        this.apiClientSSLParams = apiClientSSLParams;
        return this;
    }

    public ApiClientHttpRequest<TRequest, TResponse> setApiClientSSLBase64Params(ApiClientSSLBase64Params apiClientSSLBase64Params) {
        this.apiClientSSLBase64Params = apiClientSSLBase64Params;
        return this;
    }

    public ApiClientHttpRequest<TRequest, TResponse> setFollowHttpRedirect(boolean followHttpRedirect) {
        this.followHttpRedirect = followHttpRedirect;
        return this;
    }
    //endregion

    public ApiClientHttpRequest<TRequest, TResponse> registerHttpResponseCodeProcessor(IApiClientHttpResponseCodeProcessor<TResponse> processor) {
        httpResponseCodeProcessors.put(processor.getHttpCode(), processor);
        return this;
    }

    public ApiClientHttpRequest<TRequest, TResponse> registerHttpRequestHeader(String headerName, String headerValue) {
        headers.put(headerName, headerValue);
        return this;
    }

    public ApiClientHttpRequest<TRequest, TResponse> setResponseBodyCharset(Charset charset) {
        this.responseBodyCharset = charset;
        return this;
    }
}

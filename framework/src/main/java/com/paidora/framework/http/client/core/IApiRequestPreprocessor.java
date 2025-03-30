package com.paidora.framework.http.client.core;

import com.paidora.framework.exceptions.UnexpectedBehaviourException;
import org.apache.hc.core5.http.HttpRequest;


public interface IApiRequestPreprocessor<TRequest> {
    String preprocessHttpRequest(HttpRequest httpRequest, ApiClientHttpRequest<TRequest, ?> request, String body) throws UnexpectedBehaviourException;
}

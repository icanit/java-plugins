package com.paidora.framework.http.client.core;

import com.paidora.framework.exceptions.UnexpectedBehaviourException;
import org.apache.hc.core5.http.HttpResponse;

public interface IApiResponseStringBodyPreprocessor {
    String preprocessHttpResponse(HttpResponse response, String body) throws UnexpectedBehaviourException;
}

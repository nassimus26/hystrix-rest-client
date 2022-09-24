package org.nas.hystrix.rest.client.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import feign.Headers;
import feign.RequestLine;
import feign.Response;
import org.nas.hystrix.rest.client.annotation.HystrixRestClient;
import org.nas.hystrix.rest.client.model.Message;

/**
 * Created by Nassim MOUALEK on 20/09/2018.
 */
@HystrixRestClient(name = "clientFallbackAutoDetected")
public interface TestClientWithFallbackAutoDetectedService {
    @RequestLine("GET /message/fallback")
    Response getMessage() throws JsonProcessingException;

    @RequestLine("POST /message/fallback")
    @Headers("Content-Type: application/json;charset=utf-8")
    Response postMessage(Message hello);

}

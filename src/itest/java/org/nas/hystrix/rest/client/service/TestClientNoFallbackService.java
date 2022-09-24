package org.nas.hystrix.rest.client.service;

import feign.Headers;
import feign.RequestLine;
import feign.Response;
import org.nas.hystrix.rest.client.annotation.HystrixRestClient;
import org.nas.hystrix.rest.client.feign.HystrixRestClientEncoder;
import org.nas.hystrix.rest.client.model.Message;


/**
 * Created by Nassim MOUALEK on 20/09/2018.
 */
@HystrixRestClient(name = "clientNoFallback", encoder = HystrixRestClientEncoder.FILE_ENCODER, semaphoreMaxConcurrentRequests = 20, useThreads = true)
public interface TestClientNoFallbackService {
    @RequestLine("GET /message")
    Response getMessage();

    @RequestLine("GET /responseMessage")
    Message getResponseAsMessage();

    @RequestLine("GET /fail")
    Response getFailMessage();

    @RequestLine("HEAD /head")
    Response head();

    @RequestLine("POST /message")
    @Headers("Content-Type: application/json;charset=utf-8")
    Response postMessage(Message hello);

    @RequestLine("GET /not-found")
    Response getNotFound();


    @RequestLine("GET /error")
    Response error();
}

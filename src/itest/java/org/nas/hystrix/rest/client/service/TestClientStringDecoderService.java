package org.nas.hystrix.rest.client.service;

import feign.RequestLine;
import feign.Response;
import org.nas.hystrix.rest.client.annotation.HystrixRestClient;
import org.nas.hystrix.rest.client.feign.decoders.HystrixRestClientDecoder;

/**
 * Created by Nassim MOUALEK on 20/09/2018.
 */
@HystrixRestClient(name = "clientStringDecoder", decoder = HystrixRestClientDecoder.STRING_DECODER,
        fallback = TestClientStringDecoderServiceFallback.class,
        hystrixTimeoutMilliseconds = 1000, connectTimeoutMilliseconds = 100, readTimeoutMilliseconds = 500)
public interface TestClientStringDecoderService {

    @RequestLine("GET /responseString")
    String getStringResponse();

    @RequestLine("GET /responseMessage")
    Response getMessageResponse();

    @RequestLine("GET /timeout")
    Response timeout();
}
package org.nas.hystrix.rest.client.service;

import feign.RequestLine;
import feign.Response;
import org.nas.hystrix.rest.client.annotation.HystrixRestClient;
import org.nas.hystrix.rest.client.feign.HystrixRestClientEncoder;
import org.nas.hystrix.rest.client.feign.decoders.HystrixRestClientDecoder;

/**
 * Created by Nassim MOUALEK on 20/09/2018.
 */
@HystrixRestClient(value = "clientWithFallbackNoParam", decoder = HystrixRestClientDecoder.JACKSON_DECODER,
        encoder = HystrixRestClientEncoder.JACKSON_ENCODER)
public interface TestClientNoFallbackWithoutParamService {
    @RequestLine("GET /message")
    Response getMessage();
}

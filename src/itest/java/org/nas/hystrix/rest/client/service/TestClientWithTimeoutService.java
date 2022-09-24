package org.nas.hystrix.rest.client.service;

import feign.RequestLine;
import feign.Response;
import org.nas.hystrix.rest.client.annotation.HystrixRestClient;
import org.nas.hystrix.rest.client.service.fallback.TestClientWithTimeoutServiceFallback;

/**
 * Rest client for testing timeouts.
 */
@HystrixRestClient(name = "withTimeout", fallback = TestClientWithTimeoutServiceFallback.class, connectTimeoutMilliseconds = 5000,
        readTimeoutMilliseconds = 1, hystrixTimeoutMilliseconds = 5000)
public interface TestClientWithTimeoutService {

    @RequestLine("GET /timeouts")
    Response getTimeouts();

}

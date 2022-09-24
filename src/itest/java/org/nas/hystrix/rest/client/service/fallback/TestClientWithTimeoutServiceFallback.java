package org.nas.hystrix.rest.client.service.fallback;

import feign.Request;
import feign.Response;
import org.nas.hystrix.rest.client.service.FeginRequestUtil;
import org.nas.hystrix.rest.client.service.TestClientWithTimeoutService;
import org.springframework.http.HttpStatus;

import java.util.Collections;

/**
 * {@link TestClientWithTimeoutService} fallback.
 */
public class TestClientWithTimeoutServiceFallback implements TestClientWithTimeoutService {
    @Override
    public Response getTimeouts() {
        return Response.builder()
                .request(FeginRequestUtil.fakeRequest(Request.HttpMethod.GET))
                .status(HttpStatus.GATEWAY_TIMEOUT.value())
                .headers(Collections.singletonMap("Key", Collections.singletonList("In Fallback")))
                .build();
    }
}

package org.nas.hystrix.rest.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import feign.RequestLine;
import feign.Response;
import org.nas.hystrix.rest.client.annotation.HystrixRestClient;

/**
 * Created by Nassim MOUALEK on 20/09/2018.
 */
@HystrixRestClient(name = "clientFallbackAutoDetected", url = "${testparams.url}")
public interface TestClient {

    @RequestLine("GET /message/fallback")
    Response getMessage() throws JsonProcessingException;

}

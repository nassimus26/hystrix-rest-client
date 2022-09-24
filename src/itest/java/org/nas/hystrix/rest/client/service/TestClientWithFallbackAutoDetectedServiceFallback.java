package org.nas.hystrix.rest.client.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Request;
import feign.Response;
import org.nas.hystrix.rest.client.TestConstants;
import org.nas.hystrix.rest.client.model.Message;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;

import static org.nas.hystrix.rest.client.service.FeginRequestUtil.fakeRequest;

/**
 * Created by Nassim MOUALEK on 20/09/2018.
 */
public class TestClientWithFallbackAutoDetectedServiceFallback implements TestClientWithFallbackAutoDetectedService {
    @Autowired
    private ObjectMapper defaultObjectMapper;

    @Override
    public Response getMessage() throws JsonProcessingException {
        byte[] bytes = defaultObjectMapper.writeValueAsBytes(new Message(TestConstants.HRC_TEST));
        return Response.builder().request(fakeRequest(Request.HttpMethod.GET)).status(720).body(bytes).headers(new HashMap<>()).build();
    }

    @Override
    public Response postMessage(Message hello) {
        return Response.builder().request(fakeRequest(Request.HttpMethod.POST)).status(720).headers(new HashMap<>()).build();
    }


}

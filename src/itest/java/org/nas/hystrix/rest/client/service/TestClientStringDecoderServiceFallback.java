package org.nas.hystrix.rest.client.service;

import feign.Request;
import feign.Response;

import java.util.HashMap;

import static org.nas.hystrix.rest.client.service.FeginRequestUtil.fakeRequest;

/**
 * Created by Nassim MOUALEK on 24/09/2018.
 */
public class TestClientStringDecoderServiceFallback implements TestClientStringDecoderService {
    @Override
    public String getStringResponse() {
        return null;
    }

    @Override
    public Response getMessageResponse() {
        return null;
    }

    @Override
    public Response timeout() {
        return Response.builder().request(fakeRequest(Request.HttpMethod.GET)).status(200).headers(new HashMap<>()).build();
    }
}

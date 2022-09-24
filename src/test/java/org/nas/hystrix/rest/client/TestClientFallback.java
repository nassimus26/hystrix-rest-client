package org.nas.hystrix.rest.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;

/**
 * Created by Nassim MOUALEK on 20/09/2018.
 */
public class TestClientFallback implements TestClient {
    @Autowired
    private ObjectMapper defaultObjectMapper;

    @Override
    public Response getMessage() throws JsonProcessingException {
        return Response.builder().status(720).headers(new HashMap<>()).build();
    }

}

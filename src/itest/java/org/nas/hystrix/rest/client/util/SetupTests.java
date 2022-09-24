package org.nas.hystrix.rest.client.util;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

import java.util.Map;

/**
 * Created by Nassim MOUALEK on 17/09/2018.
 */
public class SetupTests implements TestInstancePostProcessor, AfterAllCallback {

    private Map<String, String> envsMap;

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) {
        int port = PortUtil.nextPort();
        System.out.println("******************************************************");
        System.out.println("port " + port);
        envsMap = Map.of(
                "server.port", String.valueOf(port),
                "hystrix-rest-client.clientNoFallback.url", "http://localhost:" + port,
                "hystrix-rest-client.clientFallback.url", "localhost:" + port,
                "hystrix-rest-client.clientFallbackAutoDetected.url", "localhost:1111",
                "hystrix-rest-client.client.url", "https://localhost:" + port,
                "hystrix-rest-client.clientWithFallbackNoParam.url", "http://localhost:" + port,
                "hystrix-rest-client.clientNoFallbackMultiPart.url", "http://localhost:" + port,
                "hystrix-rest-client.clientNoFallbackFile.url", "http://localhost:" + port,
                "hystrix-rest-client.clientStringDecoder.url", "http://localhost:" + port,
                "hystrix-rest-client.withTimeout.url", "http://localhost:" + port);
        System.getProperties().putAll(envsMap);

    }

    @Override
    public void afterAll(ExtensionContext context) {
        envsMap.keySet().forEach(k -> System.getProperties().remove(k));
    }

}

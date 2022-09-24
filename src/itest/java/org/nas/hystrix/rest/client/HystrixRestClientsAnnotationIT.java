package org.nas.hystrix.rest.client;

import feign.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.nas.hystrix.rest.client.annotation.EnableHystrixRestClient;
import org.nas.hystrix.rest.client.config.HystrixRestClientConfig;
import org.nas.hystrix.rest.client.model.Message;
import org.nas.hystrix.rest.client.service.TestClientNoFallbackService;
import org.nas.hystrix.rest.client.util.SetupTests;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * Created by Nassim MOUALEK on 17/09/2018.
 */
@ExtendWith({SetupTests.class, SpringExtension.class})
@SpringBootTest(webEnvironment = DEFINED_PORT, classes = HystrixRestClientsAnnotationIT.Application.class)
public class HystrixRestClientsAnnotationIT {

    @Autowired
    private TestClientNoFallbackService testClientNoFallback;

    @Test
    public void testGetQueryWithoutFallback() throws IOException {
        //When
        Response response = this.testClientNoFallback.getMessage();
        //Then
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(200);

    }

    @Test
    public void testGetQueryWithoutFallbackDefaultConverter() throws IOException {
        //When
        Message response = this.testClientNoFallback.getResponseAsMessage();
        //Then
        assertThat(response).isNotNull();

    }

    /**
     * Application stater.
     */
    @SpringBootApplication
    @RestController
    @ComponentScan(basePackageClasses = {HystrixRestClientConfig.class})
    @EnableHystrixRestClient(basePackages = {"org.nas.hystrix.rest.client.service"}, clients = {TestClientNoFallbackService.class})
    protected static class Application {

        @RequestMapping(method = GET, path = "/message")
        public Message getMessage() {
            return new Message(TestConstants.HRC_TEST);
        }

        @RequestMapping(method = GET, path = "/responseMessage")
        public Message getResponseAsMessage() {
            return new Message(TestConstants.HRC_TEST);
        }

    }

}

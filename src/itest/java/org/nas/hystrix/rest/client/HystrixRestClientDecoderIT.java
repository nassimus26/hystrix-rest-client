package org.nas.hystrix.rest.client;

import feign.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.nas.hystrix.rest.client.annotation.EnableHystrixRestClient;
import org.nas.hystrix.rest.client.config.HystrixRestClientConfig;
import org.nas.hystrix.rest.client.model.Message;
import org.nas.hystrix.rest.client.service.TestClientStringDecoderService;
import org.nas.hystrix.rest.client.util.SetupTests;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.MultipartAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.bind.annotation.GetMapping;
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
@SpringBootTest(webEnvironment = DEFINED_PORT, classes = HystrixRestClientDecoderIT.Application.class)
public class HystrixRestClientDecoderIT {

    @Autowired
    private TestClientStringDecoderService testClientStringDecoderService;

    @Test
    public void testDecoderString() throws IOException {
        //When
        String response = testClientStringDecoderService.getStringResponse();
        //Then
        assertThat(response).isNotNull();
        assertThat(response).isEqualTo(TestConstants.HRC_TEST);

    }

    @Test
    public void testDecoderMessage() throws IOException {
        //When
        Response response = testClientStringDecoderService.getMessageResponse();
        //Then
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(200);

    }


    @Test
    public void testDecoderTimeout() throws IOException {
        //When
        Response response = testClientStringDecoderService.timeout();
        //Then
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(200);

    }


    /**
     * Application stater.
     */
    @SpringBootApplication
    @RestController
    @ComponentScan(basePackageClasses = {HystrixRestClientConfig.class})
    @EnableHystrixRestClient(basePackages = {"org.nas.hystrix.rest.client.service"})
    @Import(value = MultipartAutoConfiguration.class)
    protected static class Application {

        @GetMapping(value = "/responseString")
        public String responseString() {
            return TestConstants.HRC_TEST;

        }

        @GetMapping(value = "/responseMessage")
        public Message responseMessage() {
            return new Message(TestConstants.HRC_TEST);

        }

        @RequestMapping(method = GET, path = "/timeout")
        public void timeout() throws InterruptedException {
            Thread.sleep(1400);
        }

    }

}

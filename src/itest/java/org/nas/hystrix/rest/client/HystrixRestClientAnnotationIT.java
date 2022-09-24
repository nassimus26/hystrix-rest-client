package org.nas.hystrix.rest.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.nas.hystrix.rest.client.annotation.EnableHystrixRestClient;
import org.nas.hystrix.rest.client.model.Message;
import org.nas.hystrix.rest.client.service.TestClientNoFallbackService;
import org.nas.hystrix.rest.client.service.TestClientNoFallbackWithoutParamService;
import org.nas.hystrix.rest.client.service.TestClientWithFallbackAutoDetectedService;
import org.nas.hystrix.rest.client.util.SetupTests;
import org.nas.hystrix.rest.client.util.SpringBootAppRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

/**
 * Created by Nassim MOUALEK on 17/09/2018.
 */
@ExtendWith({SetupTests.class, SpringExtension.class})
@SpringBootTest(webEnvironment = DEFINED_PORT, classes = HystrixRestClientAnnotationIT.Application.class)
public class HystrixRestClientAnnotationIT {

    @Autowired
    private TestClientNoFallbackService testClientNoFallback;
    @Autowired
    private TestClientWithFallbackAutoDetectedService testClientWithFallbackAutoDetected;
    @Autowired
    private TestClientNoFallbackWithoutParamService testClientNoFallbackWithoutParamService;
    @Autowired
    private ObjectMapper defaultObjectMapper;

    @Test
    public void testGetQueryWithoutFallback() throws IOException {
        //When
        Response response = this.testClientNoFallback.getMessage();
        //Then
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(200);
        Message result = defaultObjectMapper.readValue(response.body().asInputStream(), Message.class);
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(TestConstants.HRC_TEST);
    }

    @Test
    public void testGetQueryWithoutFallbackWithoutParam() throws IOException {
        //When
        Response response = this.testClientNoFallbackWithoutParamService.getMessage();
        //Then
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(200);
        Message result = defaultObjectMapper.readValue(response.body().asInputStream(), Message.class);
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(TestConstants.HRC_TEST);
    }


    @Test
    public void testPostQueryWithoutFallback() {
        //When
        Response response = testClientNoFallback.postMessage(new Message(TestConstants.HRC_TEST));
        //Then
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(200);

    }

    @Test
    public void testHeadQueryWithoutFallback() {
        //When
        Response response = testClientNoFallback.head();
        //Then
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(200);

    }

    @Test
    public void testGetQueryFailWithoutFallback() throws IOException {
        //When
        Response response = this.testClientNoFallback.getFailMessage();
        //Then
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(500);
        NullPointerException result = defaultObjectMapper.readValue(response.body().asInputStream(), NullPointerException.class);
        assertThat(result.getMessage()).isEqualTo("always fails");

    }

    @Test
    public void testGetQueryWithFallbackAutoDetected() throws IOException {
        //When
        Response response = this.testClientWithFallbackAutoDetected.getMessage();
        //Then
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(720);
        Message result = defaultObjectMapper.readValue(response.body().asInputStream(), Message.class);
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(TestConstants.HRC_TEST);
    }

    @Test
    public void testPostQueryWithFallbackAutoDetected() {
        //When
        Response response = this.testClientWithFallbackAutoDetected.postMessage(new Message(TestConstants.HRC_TEST));
        //Then
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(720);
    }

    /**
     * Application stater.
     */
    @SpringBootApplication
    @RestController
    //@ComponentScan(basePackageClasses = { HystrixRestClientConfig.class })
    @EnableHystrixRestClient(basePackages = {"org.nas.hystrix.rest.client.service"}, clients = {TestClientNoFallbackService.class})
    public static class Application {
        public static void main(String[] args) {
            SpringBootAppRunner.run(Application.class);
        }

        @GetMapping(path = "/message")
        public Message getMessage() {
            return new Message(TestConstants.HRC_TEST);
        }

        @PostMapping(path = "/message")
        public ResponseEntity postMessage(@RequestBody Message messageTest) {
            if (TestConstants.HRC_TEST.equals(messageTest.getName())) {
                return ResponseEntity.ok().build();
            }
            throw new IllegalArgumentException("Fail post message");
        }

        @RequestMapping(method = RequestMethod.HEAD, path = "/head")
        ResponseEntity<Void> head() {
            return ResponseEntity.ok().build();
        }

        @GetMapping(path = "/fail")
        String fail() {
            throw new NullPointerException("always fails");
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity handleError(HttpServletRequest req, Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex);
        }
    }

}

package org.nas.hystrix.rest.client;

import feign.Response;
import lombok.Getter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.nas.hystrix.rest.client.annotation.EnableHystrixRestClient;
import org.nas.hystrix.rest.client.model.Message;
import org.nas.hystrix.rest.client.service.TestClientWithTimeoutService;
import org.nas.hystrix.rest.client.util.PortUtil;
import org.nas.hystrix.rest.client.util.SetupTests;
import org.nas.hystrix.rest.client.util.SpringBootAppRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

/**
 * Integration tests with client {@link TestClientWithTimeoutService} on timeouts.
 */
@ExtendWith({SetupTests.class, SpringExtension.class})
@SpringBootTest(webEnvironment = DEFINED_PORT, classes = HystrixRestClientTimeoutIT.Application.class)
public class HystrixRestClientTimeoutIT {
    @Autowired
    private ConfigurableApplicationContext context;

    @AfterAll
    public void after() {
        Application application = context.getBean(Application.class);
        application.getLatch().countDown();
        context.close();
    }

    @Test
    public void should_use_parametrized_timeouts_and_go_to_fallback() {
        // Given
        System.getProperties().replace("server.port", String.valueOf(PortUtil.nextPort()));
        context = SpringBootAppRunner.run(Application.class);
        TestClientWithTimeoutService testClientWithTimeoutService = context.getBean(TestClientWithTimeoutService.class);

        // When
        Response response = testClientWithTimeoutService.getTimeouts();

        // Then
        assertThat(response.headers().get("Key"))
                .containsExactly("In Fallback");
        assertThat(response.status())
                .isEqualTo(HttpStatus.GATEWAY_TIMEOUT.value());
    }

    @Test
    public void should_go_to_fallback_method_with_too_low_read_timeout() {
        // Given
        System.getProperties().replace("server.port", String.valueOf(PortUtil.nextPort()));
        context = SpringBootAppRunner.runWithEnvs(Application.class,
                new HashMap<>() {{
                    put("hystrix-rest-client.withTimeout.connectTimeoutMilliseconds", "5000");
                    put("hystrix-rest-client.withTimeout.readTimeoutMilliseconds", "1");
                    put("hystrix-rest-client.withTimeout.hystrixTimeoutMilliseconds", "5000");
                }});
        TestClientWithTimeoutService testClientWithTimeoutService = context.getBean(TestClientWithTimeoutService.class);

        // When
        Response response = testClientWithTimeoutService.getTimeouts();

        // Then
        assertThat(response.headers().get("Key"))
                .containsExactly("In Fallback");
        assertThat(response.status())
                .isEqualTo(HttpStatus.GATEWAY_TIMEOUT.value());
    }

    @Test
    public void should_throw_exception_when_hystrix_timeout_is_lower_than_connect_timeout() throws InterruptedException {
        // Given
        System.getProperties().replace("server.port", String.valueOf(PortUtil.nextPort()));
        context = SpringBootAppRunner.runWithEnvs(Application.class,
                new HashMap<String, Object>() {{
                    put("hystrix-rest-client.withTimeout.connectTimeoutMilliseconds", "11");
                    put("hystrix-rest-client.withTimeout.readTimeoutMilliseconds", "1");
                    put("hystrix-rest-client.withTimeout.hystrixTimeoutMilliseconds", "10");
                }});

        // Then
        assertThatThrownBy(() -> context.getBean(TestClientWithTimeoutService.class))
                .hasCauseExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Hystrix timeout is lower than connect timeout (10 vs 11).");
    }

    @Test
    public void should_throw_exception_when_hystrix_timeout_is_lower_than_read_timeout() throws InterruptedException {
        // Given
        System.getProperties().replace("server.port", String.valueOf(PortUtil.nextPort()));
        context = SpringBootAppRunner.runWithEnvs(Application.class,
                new HashMap<>() {{
                    put("hystrix-rest-client.withTimeout.connectTimeoutMilliseconds", "1");
                    put("hystrix-rest-client.withTimeout.readTimeoutMilliseconds", "11");
                    put("hystrix-rest-client.withTimeout.hystrixTimeoutMilliseconds", "10");
                }});

        // Then
        assertThatThrownBy(() -> context.getBean(TestClientWithTimeoutService.class))
                .hasCauseExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Hystrix timeout is lower than read timeout (10 vs 11).");
    }

    @RestController
    @EnableAutoConfiguration
    @EnableHystrixRestClient(clients = {TestClientWithTimeoutService.class})
    static class Application {

        @Getter
        private final CountDownLatch latch = new CountDownLatch(1);

        @GetMapping("/timeouts")
        public Message getTimeout() throws InterruptedException {
            latch.await();
            return new Message(TestConstants.HRC_TEST);
        }

    }

}

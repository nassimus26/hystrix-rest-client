package org.nas.hystrix.rest.client.exceptions;

import com.netflix.hystrix.exception.HystrixRuntimeException;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.concurrent.TimeoutException;

import static com.netflix.hystrix.exception.HystrixRuntimeException.FailureType.*;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.nas.hystrix.rest.client.exceptions.HystrixRestClientMessages.Message;
import static org.nas.hystrix.rest.client.exceptions.HystrixRestClientMessages.builder;

/**
 * HystrixRestClientExceptionTest.java.
 *
 * @author Nassim MOUALEK.
 */
public class HystrixRestClientExceptionTest {

    @Test
    public void testRestClientException() {
        HystrixRestClientException restClientException = new HystrixRestClientException(500);
        assertThat(restClientException.getMessage()).isEqualTo("Unexpected error");
        assertThat(restClientException.getStatus()).isEqualTo(500);
        assertThat(restClientException.getMessages()).isNull();

        //Given
        List<Message> listMessage = asList(Message.builder().code("err.func").message("error message").build());
        HystrixRestClientMessages rcMessages = builder().type("TECHNICAL").messages(listMessage).build();

        HystrixRestClientException restClientException2 = new HystrixRestClientException(500, rcMessages);
        assertThat(restClientException2.getStatus()).isEqualTo(500);
        assertThat(restClientException2.getMessages()).isNotNull();

        HystrixRestClientException restClientException3 = new HystrixRestClientException(500);
        assertThat(restClientException3.getStatus()).isEqualTo(500);
        assertThat(restClientException3.getMessages()).isNull();
        assertThat(restClientException3.getMessage()).isEqualTo("Unexpected error");

    }

    @Test
    public void testRestClientExceptionUnwrapSupplier() throws HystrixRestClientException {

        assertThat(HystrixRestClientException.unwrap(() -> true)).isTrue();

    }

    @Test
    public void testRestClientExceptionUnwrapSupplierHystrix() {

        //When
        Throwable thrown = catchThrowable(() -> HystrixRestClientException.unwrap(() -> {
                    throw new HystrixRuntimeException(BAD_REQUEST_EXCEPTION, null, "Timeout", new TimeoutException("Timeout"), null);
                }
        ));

        //Then
        assertThat(thrown).isInstanceOf(HystrixRuntimeException.class).hasMessageContaining("Timeout");

    }

    @Test
    public void testRestClientExceptionUnwrapSupplierRuntime() {

        //When
        Throwable thrown = catchThrowable(() -> HystrixRestClientException.unwrap(() -> {
                    throw new RuntimeException("rest exception");
                }
        ));

        //Then
        assertThat(thrown).isInstanceOf(RestClientException.class).hasMessageContaining("Cannot call REST endpoint");

    }

    @Test
    public void testRestClientExceptionUnwrapRunnableBadRequest() {

        //When
        Throwable thrown = catchThrowable(() -> HystrixRestClientException.unwrap(() -> {
            throw new HystrixRuntimeException(BAD_REQUEST_EXCEPTION, null, "BadRequest", new RuntimeException("BadRequest"), null);
        }));

        //Then
        assertThat(thrown).isInstanceOf(HystrixRuntimeException.class).hasMessageContaining("BadRequest");

    }

    @Test
    public void testRestClientExceptionUnwrapRunnableTimeout() {

        //When
        Throwable thrown = catchThrowable(() -> HystrixRestClientException.unwrap(() -> {
            throw new HystrixRuntimeException(TIMEOUT, null, "Timeout", new TimeoutException(""), null);
        }));

        //Then
        assertThat(thrown).isInstanceOf(HystrixRestClientException.class).hasMessageContaining("Request Time-out");

    }


    @Test
    public void testRestClientExceptionUnwrapRunnableShortCircuit() {

        //When
        Throwable thrown = catchThrowable(() -> HystrixRestClientException.unwrap(() -> {
            throw new HystrixRuntimeException(SHORTCIRCUIT, null, "short circuit", new RuntimeException("short circuit"), null);
        }));

        //Then
        assertThat(thrown).isInstanceOf(HystrixRuntimeException.class).hasMessageContaining("short circuit");

    }


    @Test
    public void testRestClientExceptionUnwrapRunnableRuntime() {

        //When
        Throwable thrown = catchThrowable(() -> HystrixRestClientException.unwrap(() -> {
            throw new RuntimeException("rest exception");
        }));

        //Then
        assertThat(thrown).isInstanceOf(RestClientException.class).hasMessageContaining("Cannot call REST endpoint");

    }

}

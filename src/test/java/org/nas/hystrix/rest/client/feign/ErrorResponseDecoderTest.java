package org.nas.hystrix.rest.client.feign;

import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Test;
import org.nas.hystrix.rest.client.exceptions.HystrixRestClientException;
import org.nas.hystrix.rest.client.feign.decoders.ErrorResponseDecoder;
import org.nas.hystrix.rest.client.utils.FeginRequestUtil;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by Nassim MOUALEK on 24/09/2018.
 */
public class ErrorResponseDecoderTest {

    @Test
    public void testTechnicalException() {
        //Given
        String technicalMessage = "technical Message";
        ErrorResponseDecoder errorResponseDecoder = new ErrorResponseDecoder();
        Response response = Response.builder().request(FeginRequestUtil.fakeRequest(Request.HttpMethod.GET)).body(technicalMessage.getBytes()).status(500)
                .headers(new HashMap<>()).build();

        //When
        Exception exception = errorResponseDecoder.decode("", response);
        //Then
        assertThat(exception).isInstanceOf(HystrixRestClientException.class);
        assertThat(exception.getMessage()).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(technicalMessage);

    }

    @Test
    public void testFunctionalException() {
        //Given
        String functionalMessage = "functional Message";
        ErrorResponseDecoder errorResponseDecoder = new ErrorResponseDecoder();
        Response response = Response.builder().request(FeginRequestUtil.fakeRequest(Request.HttpMethod.GET)).body(functionalMessage.getBytes()).status(400)
                .headers(new HashMap<>()).build();

        //When
        Exception exception = errorResponseDecoder.decode("", response);
        //Then
        assertThat(exception).isInstanceOf(HystrixRestClientException.class);
        assertThat(exception.getMessage()).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(functionalMessage);

    }

    @Test
    public void testDefaultException() throws UnsupportedEncodingException {
        //Given
        String defaultMessage = "default Message";
        ErrorResponseDecoder errorResponseDecoder = new ErrorResponseDecoder();
        Response response =
                Response.builder().request(FeginRequestUtil.fakeRequest(Request.HttpMethod.GET))
                        .body(defaultMessage.getBytes("UTF-8")).status(350).headers(new HashMap<>()).build();

        //When
        Exception exception = errorResponseDecoder.decode("", response);
        //Then
        assertThat(exception).isInstanceOf(HystrixRestClientException.class);
        assertThat(exception.getMessage()).isNotNull();
        assertThat(exception.getMessage()).contains(defaultMessage);

    }

    @Test
    public void testDefaultExceptionNoBody() throws UnsupportedEncodingException {
        //Given
        ErrorResponseDecoder errorResponseDecoder = new ErrorResponseDecoder();
        Response response = Response.builder().request(FeginRequestUtil.fakeRequest(Request.HttpMethod.GET)).body("".getBytes("UTF-8")).status(350)
                .headers(new HashMap<>()).build();

        //When
        Exception exception = errorResponseDecoder.decode("", response);
        //Then
        assertThat(exception).isInstanceOf(HystrixRestClientException.class);
        assertThat(exception.getMessage()).isNotNull();

    }

    @Test
    public void testDecodeErrorResponse() throws UnsupportedEncodingException {
        //Given
        ErrorResponseDecoder errorResponseDecoder = new ErrorResponseDecoder();
        Response response = Response.builder().request(FeginRequestUtil.fakeRequest(Request.HttpMethod.GET))
                .body("{\"type\":\"FUNCTIONAL\", \"messages\": [{\"code\":\"code\",\"message\":\"message\"}]}"
                        .getBytes("UTF-8")).status(350).headers(new HashMap<>()).build();

        //When
        Exception exception = errorResponseDecoder.decode("", response);
        //Then
        assertThat(exception).isInstanceOf(HystrixRestClientException.class);
        HystrixRestClientException restClientException = (HystrixRestClientException) exception;
        assertThat(restClientException.getMessages().getType()).isEqualTo("FUNCTIONAL");
        assertThat(restClientException.getMessages().getMessages()).hasSize(1);

        assertThat(restClientException.getMessages().getMessages().get(0).getCode()).isEqualTo("code");
        assertThat(restClientException.getMessages().getMessages().get(0).getMessage()).isEqualTo("message");

    }

}

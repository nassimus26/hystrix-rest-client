package org.nas.hystrix.rest.client.hystrix;

import feign.Request;
import feign.RetryableException;
import org.junit.jupiter.api.Test;
import org.nas.hystrix.rest.client.feign.HystrixRestClientRetryer;
import org.nas.hystrix.rest.client.utils.FeginRequestUtil;

import java.sql.Date;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * RestClientRetryerTest.java.
 *
 * @author Nassim MOUALEK.
 */
public class HystrixRestClientRetryerTest {
    private RetryableException retryableException = new RetryableException(404, "Exception", Request.HttpMethod.GET, Date.from(Instant.now()),
            FeginRequestUtil.fakeRequest(Request.HttpMethod.GET));

    @Test
    public void testRestClientRetryerWithoutRetry() {
        //Given
        HystrixRestClientRetryer restClientRetryer = new HystrixRestClientRetryer(0);

        //When
        Throwable thrown = catchThrowable(() -> restClientRetryer.continueOrPropagate(retryableException));

        //Then
        assertThat(thrown).isInstanceOf(RetryableException.class).hasMessageContaining("Exception");

    }

    @Test
    public void testRestClientRetryerWithoutRetryCloning() {

        //When
        Throwable thrown = catchThrowable(() -> new HystrixRestClientRetryer(0).clone().continueOrPropagate(retryableException));

        //Then
        assertThat(thrown).isInstanceOf(RetryableException.class).hasMessageContaining("Exception");

    }

    @Test
    public void testRestClientRetryerWithRetry() {
        //Given
        HystrixRestClientRetryer restClientRetryer = new HystrixRestClientRetryer(1);

        //When
        restClientRetryer.continueOrPropagate(retryableException);
        Throwable thrown = catchThrowable(() -> restClientRetryer.continueOrPropagate(retryableException));

        //Then
        assertThat(thrown).isInstanceOf(RetryableException.class).hasMessageContaining("Exception");

    }

    @Test
    public void testRestClientRetryerWithRetryCloning() {
        //Given
        HystrixRestClientRetryer cloneRestClientRetryer = (HystrixRestClientRetryer) new HystrixRestClientRetryer(1).clone();

        //When
        cloneRestClientRetryer.continueOrPropagate(retryableException);
        Throwable thrown = catchThrowable(() -> cloneRestClientRetryer.continueOrPropagate(retryableException));

        //Then
        assertThat(thrown).isInstanceOf(RetryableException.class).hasMessageContaining("Exception");

    }

}

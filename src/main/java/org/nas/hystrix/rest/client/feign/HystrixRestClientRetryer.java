package org.nas.hystrix.rest.client.feign;

import feign.RetryableException;
import feign.Retryer;
import lombok.extern.slf4j.Slf4j;

/**
 * RestClientRepeater.java.
 * <p>
 * Repeat when request is KO.
 *
 * @author Nassim MOUALEK.
 */
@Slf4j
public class HystrixRestClientRetryer implements Retryer {
    private final int maxAttempts;
    private int attempt;

    public HystrixRestClientRetryer(int maxAttempts) {
        this.maxAttempts = maxAttempts;
        this.attempt = 0;
    }

    @Override
    public void continueOrPropagate(RetryableException e) {
        if (this.attempt++ >= this.maxAttempts) {
            throw e;
        }
    }

    @Override
    @SuppressWarnings({"squid:S2975"})
    public Retryer clone() {

        try {
            return (Retryer) super.clone();
        } catch (CloneNotSupportedException c) {
            throw new IllegalStateException("Unable to clone RestClientRetryer object.", c);
        }

    }

}

package org.nas.hystrix.rest.client.hystrix;

import feign.hystrix.FallbackFactory;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by Nassim MOUALEK on 17/09/2018.
 */
@AllArgsConstructor
@Slf4j
public final class HystrixRestClientFallbackFactory implements FallbackFactory {

    private Object fallback;

    /**
     * Create Fallback class.
     *
     * @param cause exception returned by client
     * @return Fallback class
     */

    @Override
    public Object create(Throwable cause) {
        log.debug(cause.getMessage(), cause);
        return this.fallback;
    }
}

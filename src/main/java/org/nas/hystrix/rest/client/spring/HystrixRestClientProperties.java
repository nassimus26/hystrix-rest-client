package org.nas.hystrix.rest.client.spring;

import lombok.Getter;
import lombok.Setter;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static okhttp3.logging.HttpLoggingInterceptor.Level.NONE;

/**
 * RestClientProperties.java.
 *
 * @author Nassim MOUALEK.
 */

@Getter
@Setter
@ConfigurationProperties("hytrix-rest-client")
public class HystrixRestClientProperties {
    private int maxAttempts = 0;
    private OkhttpLog okhttp = new OkhttpLog();

    @Getter
    @Setter
    public static class OkhttpLog {
        /**
         * this properties enable or disable the log details like
         * url, method, protocol, response status and the execution time.
         */
        private boolean structuredLog = false;

        private HttpLoggingInterceptor.Level loggingLevel = NONE;

    }
}

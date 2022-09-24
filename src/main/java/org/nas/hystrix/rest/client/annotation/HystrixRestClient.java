package org.nas.hystrix.rest.client.annotation;

import feign.codec.Decoder;
import feign.codec.ErrorDecoder;
import feign.codec.StringDecoder;
import org.nas.hystrix.rest.client.feign.HystrixRestClientEncoder;
import org.nas.hystrix.rest.client.feign.decoders.ErrorResponseDecoder;
import org.nas.hystrix.rest.client.feign.decoders.HystrixRestClientDecoder;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * Declare that a REST client with that interface should be created, with the configured properties (timeout values,
 * fallback, encoders and decoders). Note that it follows convention over configuration for the fallback, and will
 * automatically pick a class with the name of the client suffixed by {@code Fallback} if it exists.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HystrixRestClient {

    int DEFAULT_HYSTRIX_TIMEOUT_MILLISECONDS = 60_000;
    int DEFAULT_CONNECT_TIMEOUT_MILLISECONDS = 10_000;
    int DEFAULT_READ_TIMEOUT_MILLISECONDS = 10_000;

    /**
     * The name of the service with optional protocol prefix.
     *
     * @return value
     */
    @AliasFor("name")
    String value() default "";

    /**
     * The service id with optional protocol prefix.
     *
     * @return name
     */
    @AliasFor("value")
    String name() default "";

    /**
     * This property sets the absolute URL or resolvable hostname (the protocol is optional).
     *
     * @return url
     */
    String url() default "";

    /**
     * This property sets the time in milliseconds after which the caller will observe a timeout and walk away from the command execution.
     * Hystrix marks the HystrixCommand as a TIMEOUT, and performs fallback logic.
     * Note that there is configuration for turning off timeouts per-command.
     * A timeout of zero is interpreted as an infinite timeout.
     *
     * @return Hystrix timeout in milliseconds
     */
    int hystrixTimeoutMilliseconds() default DEFAULT_HYSTRIX_TIMEOUT_MILLISECONDS;

    /**
     * Sets a specified connect timeout value, in milliseconds, to be used when opening a communications link to the resource referenced
     * by this url.  If the timeout expires before the connection can be established, performs fallback logic.
     * A timeout of zero is interpreted as an infinite timeout.
     *
     * @return connectTimeout in milliseconds
     */
    int connectTimeoutMilliseconds() default DEFAULT_CONNECT_TIMEOUT_MILLISECONDS;

    /**
     * Sets the read timeout to a specified timeout, in milliseconds. A non-zero value specifies the timeout when
     * reading from Input stream when a connection is established to a resource. If the timeout expires before there is data available
     * for read, performs fallback logic. A timeout of zero is interpreted as an infinite timeout.
     *
     * @return readTimeout in milliseconds
     */
    int readTimeoutMilliseconds() default DEFAULT_READ_TIMEOUT_MILLISECONDS;

    /**
     * This property indicates which isolation strategy {@code HystrixCommand.run()} executes with, one of the following
     * two choices:
     * <dl>
     *    <dt>true</dt>
     *    <dd>
     *        THREAD — it executes on a separate thread and concurrent requests are limited by the number of threads in
     *        the thread-pool
     *    </dd>
     *
     *    <dt>false</dt>
     *    <dd>
     *        SEMAPHORE — it executes on the calling thread and concurrent requests are limited by the semaphore count
     *    </dd>
     * </dl>
     *
     * @return use thread
     */
    boolean useThreads() default false;

    /**
     * Fallback class. If left void, the library will automatically pick a class with the name of the client suffixed by
     * {@code Fallback} ({@code XxxClientFallback} for {@code XxxClient}), if it exists.
     *
     * @return Class Fallback
     */
    Class<?> fallback() default void.class;

    /**
     * This property indicates the max number of concurrent requests and fallback when the semaphore strategy is used.
     *
     * @return semaphore max concurrent requests
     */
    int semaphoreMaxConcurrentRequests() default 100;

    /**
     * This property sets the encoder of the request.
     *
     * @return encoder type
     */

    HystrixRestClientEncoder encoder() default HystrixRestClientEncoder.JACKSON_ENCODER;

    /**
     * This property sets the decoder of the response.
     *
     * @return decoder type
     */

    HystrixRestClientDecoder decoder() default HystrixRestClientDecoder.JACKSON_DECODER;

    /**
     * This property sets the decoder of the error response.
     *
     * @return decoder class
     */
    Class<? extends ErrorDecoder> errorDecoder() default ErrorResponseDecoder.class;

    /**
     * This property sets the custom decoder class.
     *
     * @return custom decoder class
     */
    Class<? extends Decoder> customDecoder() default StringDecoder.class;

}

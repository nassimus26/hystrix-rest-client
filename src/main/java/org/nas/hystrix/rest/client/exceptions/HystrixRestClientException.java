package org.nas.hystrix.rest.client.exceptions;

import com.netflix.hystrix.exception.HystrixRuntimeException;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientException;

import java.util.function.Supplier;

import static java.util.Collections.singletonList;

/**
 * RestClientException.
 *
 * @author Nassim MOUALEK
 */
@Getter
public class HystrixRestClientException extends Exception {

    private final int status;
    private final HystrixRestClientMessages messages;

    public HystrixRestClientException(int status) {
        this(status, "Unexpected error");
    }

    public HystrixRestClientException(int status, HystrixRestClientMessages messages) {
        super(messages == null ? "Unexpected error" : messages.toString());
        this.status = status;
        this.messages = messages;
    }

    public HystrixRestClientException(int status, String message) {
        super(message);
        this.status = status;
        this.messages = null;
    }

    /**
     * Unwrap the RestClientException embbeded in a HystrixRuntimeException after an unsucessfull call to a remote
     * service.
     * <code>
     * Thumbnail myDTO = unwrap(() -&gt; myClient.callMyService(param1, param2);
     * </code>
     *
     * @param supplier the supplier which will call the service
     * @param <T>      the class of returned parameter
     * @return the result of the service call
     * @throws HystrixRestClientException if an error occurrs.
     */
    @SuppressWarnings("squid:S1181")
    public static <T> T unwrap(Supplier<T> supplier) throws HystrixRestClientException {
        try {
            return supplier.get();
        } catch (HystrixRuntimeException e) {
            handleHystrixRuntimeException(e);
            return null;
        } catch (Throwable e) {
            throwIfRestClientException(e);
            throw new RestClientException("Cannot call REST endpoint", e);
        }

    }

    /**
     * Unwrap the RestClientException embbeded in a HystrixRuntimeException after an unsucessfull call to a remote
     * service.
     * <code>
     * unwrap(() -&gt; myClient.callMyService(param1, param2);
     * </code>
     *
     * @param runnable the runnable which will call the service
     * @throws HystrixRestClientException if an error occurrs.
     */
    @SuppressWarnings("squid:S1181")
    public static void unwrap(Runnable runnable) throws HystrixRestClientException {
        try {
            runnable.run();
        } catch (HystrixRuntimeException e) {
            handleHystrixRuntimeException(e);
        } catch (Throwable e) {
            throwIfRestClientException(e);
            throw new RestClientException("Cannot call REST endpoint", e);
        }

    }

    private static void handleHystrixRuntimeException(HystrixRuntimeException e) throws HystrixRestClientException {

        throwIfRestClientException(e.getCause());
        if (e.getFailureType() == HystrixRuntimeException.FailureType.TIMEOUT) {

            HystrixRestClientMessages messages = HystrixRestClientMessages.builder()
                    .type("TECHNICAL")
                    .messages(singletonList(HystrixRestClientMessages.Message.builder()
                            .code("err.tech.requesttimeout")
                            .message("Request Time-out")
                            .build()))
                    .build();
            throw new HystrixRestClientException(HttpStatus.REQUEST_TIMEOUT.value(), messages);
        }
        throw e;
    }

    private static void throwIfRestClientException(Throwable exception) throws HystrixRestClientException {
        if (exception instanceof HystrixRestClientException) {
            throw (HystrixRestClientException) exception;
        }
    }

}

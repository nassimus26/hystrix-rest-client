package org.nas.hystrix.rest.client.okhttp3;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Connection;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.http.HttpStatus;

import java.io.IOException;

import static okhttp3.Protocol.HTTP_1_1;
import static org.nas.hystrix.rest.client.okhttp3.LoggingInterceptor.StatusFamily.*;

/**
 * LoggingInterceptor.java.
 *
 * @author Nassim MOUALEK.
 */
@Slf4j
@RequiredArgsConstructor
public class LoggingInterceptor implements Interceptor {

    private final HttpLoggerService httpLoggerService;

    private static StatusFamily getStatusFamily(int responseCode) {
        HttpStatus httpStatus = HttpStatus.valueOf(responseCode);
        if (httpStatus.is1xxInformational()) {
            return INFORMATIONAL;
        } else if (httpStatus.is2xxSuccessful()) {
            return SUCCESS;
        } else if (httpStatus.is3xxRedirection()) {
            return REDIRECTION;
        } else if (httpStatus.is4xxClientError()) {
            return CLIENT_ERROR;
        } else {
            return SERVER_ERROR;
        }
    }

    @Override
    @SuppressWarnings("squid:S2221")
    public Response intercept(Chain chain) throws IOException {

        Request request = chain.request();

        Connection connection = chain.connection();
        String protocol = connection != null ? connection.protocol().toString() : HTTP_1_1.toString();

        String method = request.method();
        String url = request.url() != null ? request.url().toString() : "";

        Response response = null;
        HttpRequestReport.HttpRequestReportBuilder httpRequestReportBuilder = HttpRequestReport.builder()
                .method(method)
                .protocol(protocol)
                .url(url);
        long start = System.nanoTime();

        try {
            response = chain.proceed(request);
            int codeResponse = response.code();
            httpRequestReportBuilder.codeResponse(codeResponse)
                    .statusFamily(getStatusFamily(codeResponse));
        } catch (IOException exception) {
            httpRequestReportBuilder.exception(exception.getMessage())
                    .statusFamily(SERVER_ERROR);
            throw exception;
        } finally {
            long end = System.nanoTime();
            httpLoggerService.log(httpRequestReportBuilder
                    .executionTimeMilliseconds((end - start) / 1_000_000)
                    .build());
        }
        return response;
    }


    /**
     * Status.
     */
    public enum StatusFamily {
        INFORMATIONAL,
        SUCCESS,
        REDIRECTION,
        SERVER_ERROR,
        CLIENT_ERROR
    }

    /**
     * HttpRequestReport.
     */
    @Builder
    @Data
    public static class HttpRequestReport {
        private final StatusFamily statusFamily;
        private final String method;
        private final String url;
        private final int codeResponse;
        private final String protocol;
        private final long executionTimeMilliseconds;
        private final String exception;
    }

}

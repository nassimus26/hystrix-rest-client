package org.nas.hystrix.rest.client.feign.decoders;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nas.hystrix.rest.client.exceptions.HystrixRestClientException;
import org.nas.hystrix.rest.client.exceptions.HystrixRestClientMessages;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * Created by Nassim MOUALEK on 23/09/2018.
 */
@Slf4j
@NoArgsConstructor
public class ErrorResponseDecoder implements ErrorDecoder {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @SuppressWarnings("squid:S1166")
    public Exception decode(String methodKey, Response response) {

        String stringBody;
        try (Response.Body body = response.body()) {
            stringBody = Util.toString(body.asReader());
        } catch (IOException e) {
            log.error("Cannot decode response body from {}", methodKey, e);
            return new HystrixRestClientException(response.status());
        }

        if (StringUtils.hasText(stringBody)) {
            try {
                HystrixRestClientMessages rcMessages = objectMapper.readValue(stringBody, HystrixRestClientMessages.class);
                return new HystrixRestClientException(response.status(), rcMessages);
            } catch (IOException e) {
                log.error("Cannot extract message from '{}'", stringBody);
                return new HystrixRestClientException(response.status(), stringBody);
            }
        }

        return new HystrixRestClientException(response.status());
    }

}

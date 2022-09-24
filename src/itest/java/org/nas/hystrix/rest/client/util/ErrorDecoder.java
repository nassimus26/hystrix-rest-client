package org.nas.hystrix.rest.client.util;

import feign.Response;

/**
 * Created by Nassim MOUALEK on 23/09/2018.
 */

public class ErrorDecoder implements feign.codec.ErrorDecoder {

    private feign.codec.ErrorDecoder delegate = new Default();


    @Override
    public Exception decode(String methodKey, Response response) {
        return delegate.decode(methodKey, response);
    }
}

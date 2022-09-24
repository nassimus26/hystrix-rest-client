package org.nas.hystrix.rest.client.utils;

import feign.Request;
import lombok.experimental.UtilityClass;

import java.util.HashMap;

@UtilityClass
public class FeginRequestUtil {
    public static final Request fakeRequest(Request.HttpMethod method) {
        return Request.create(method, "", new HashMap<>(), null);
    }
}

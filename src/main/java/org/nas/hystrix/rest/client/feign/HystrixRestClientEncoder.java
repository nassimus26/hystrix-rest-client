package org.nas.hystrix.rest.client.feign;

import org.springframework.web.multipart.MultipartFile;

/**
 * Created by Nassim MOUALEK on 23/09/2018.
 */
public enum HystrixRestClientEncoder {
    /**
     * Default encoder : regular REST calls (DTOs are serialized with Jackson).
     */
    JACKSON_ENCODER,
    /**
     * File encoder : to send files using the "multipart/form-data" content type.
     */
    FILE_ENCODER,
    /**
     * MultipartFile encoder : to send {@link MultipartFile} files.
     */
    MULTIPART_ENCODER
}

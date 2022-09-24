package org.nas.hystrix.rest.client.service;

import feign.Param;
import feign.RequestLine;
import feign.Response;
import org.nas.hystrix.rest.client.annotation.HystrixRestClient;
import org.nas.hystrix.rest.client.feign.HystrixRestClientEncoder;
import org.nas.hystrix.rest.client.model.UploadInfo;
import org.nas.hystrix.rest.client.model.UploadMetadata;
import org.nas.hystrix.rest.client.util.ErrorDecoder;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Created by Nassim MOUALEK on 20/09/2018.
 */
@HystrixRestClient(name = "clientNoFallbackMultiPart", semaphoreMaxConcurrentRequests = 20, encoder = HystrixRestClientEncoder.MULTIPART_ENCODER,
        errorDecoder = ErrorDecoder.class)
public interface TestClientNoFallbackMultiPartService {

    @RequestLine("POST /uploadFile")
    Response sendFile(@Param("file") MultipartFile file);

    @RequestLine("POST /uploadFile/fail")
    Response sendFileException(@Param("file") MultipartFile file);

    @RequestLine("POST /uploadArray/{folder}")
    List<UploadInfo> uploadArray(@Param("folder") String folder, @Param("files") MultipartFile[] files, @Param("metadata") UploadMetadata metadata);

}

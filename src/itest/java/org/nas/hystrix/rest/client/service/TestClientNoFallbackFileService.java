package org.nas.hystrix.rest.client.service;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import org.nas.hystrix.rest.client.HystrixRestClientFileIT.FileDesc;
import org.nas.hystrix.rest.client.annotation.HystrixRestClient;
import org.nas.hystrix.rest.client.feign.HystrixRestClientEncoder;

import java.io.File;

/**
 * TestClientNoFallbackFileService.
 *
 * @author Nassim MOUALEK
 */
@HystrixRestClient(name = "clientNoFallbackFile", encoder = HystrixRestClientEncoder.FILE_ENCODER)
public interface TestClientNoFallbackFileService {

    @RequestLine("POST /uploadFile")
    @Headers("Content-Type: multipart/form-data")
    FileDesc sendFile(@Param("file") File file);

    @RequestLine("POST /uploadFileWithFileNamedToto")
    @Headers("Content-Type: multipart/form-data")
    FileDesc sendFile2(@Param("toto") File file);

    @RequestLine("POST /uploadFile/{param}")
    @Headers("Content-Type: multipart/form-data")
    FileDesc sendFileWithParam(@Param("file") File file, @Param("param") String param);

}

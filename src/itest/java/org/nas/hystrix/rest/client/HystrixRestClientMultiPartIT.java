package org.nas.hystrix.rest.client;

import feign.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.nas.hystrix.rest.client.annotation.EnableHystrixRestClient;
import org.nas.hystrix.rest.client.config.HystrixRestClientConfig;
import org.nas.hystrix.rest.client.model.UploadInfo;
import org.nas.hystrix.rest.client.model.UploadMetadata;
import org.nas.hystrix.rest.client.service.TestClientNoFallbackMultiPartService;
import org.nas.hystrix.rest.client.util.InMemoryMultipartFile;
import org.nas.hystrix.rest.client.util.SetupTests;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.MultipartAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

/**
 * Created by Nassim MOUALEK on 17/09/2018.
 */
@ExtendWith({SetupTests.class, SpringExtension.class})
@SpringBootTest(webEnvironment = DEFINED_PORT, classes = HystrixRestClientMultiPartIT.Application.class)
public class HystrixRestClientMultiPartIT {

    @Autowired
    private TestClientNoFallbackMultiPartService testClientNoFallbackMultiPartService;

    @Test
    public void testUploadMuliPart() {

        //When
        MultipartFile file = new InMemoryMultipartFile("file.text", "file in memory".getBytes());

        Response response = testClientNoFallbackMultiPartService.sendFile(file);
        //Then
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(200);

    }

    @Test
    public void testUploadMArrayMuliPart() {
        //When
        MultipartFile file1 = new InMemoryMultipartFile("file.text1", "file in memory".getBytes());
        MultipartFile file2 = new InMemoryMultipartFile("file.text2", "file in memory".getBytes());

        List<UploadInfo> response = testClientNoFallbackMultiPartService.uploadArray("test", new MultipartFile[]{file1, file2},
                new UploadMetadata("test", 1));
        //Then
        assertThat(response).isNotNull();
        assertThat(response).size().isEqualTo(2);
        response.forEach(r -> {
            assertThat(r.getId()).isEqualTo(1);
            assertThat(r.getName()).contains("file.text");

        });

    }

    @Test
    public void testUploadMuliPartFailure() {
        //When
        MultipartFile file = new InMemoryMultipartFile("file.text", "file in memory".getBytes());

        Response response = testClientNoFallbackMultiPartService.sendFileException(file);
        //Then

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(500);
    }

    /**
     * Application stater.
     */
    @SpringBootApplication
    @RestController
    @ComponentScan(basePackageClasses = {HystrixRestClientConfig.class})
    @EnableHystrixRestClient(basePackages = {"org.nas.hystrix.rest.client.service"}, clients = {TestClientNoFallbackMultiPartService.class})
    @Import(value = MultipartAutoConfiguration.class)
    protected static class Application {

        @PostMapping(value = "/uploadFile")
        public void uploadFileHandler(@RequestParam(value = "name", required = false) String name,
                                      @RequestPart MultipartFile file) {
            if (file.getSize() == 0) {
                throw new IllegalArgumentException("File is empty");
            }

        }

        @PostMapping(value = "/uploadFile/fail")
        public void uploadFileFailHandler(@RequestPart MultipartFile file) {
            throw new NullPointerException("File is empty");
        }

        @PostMapping(path = "/uploadArray/{folder}")
        public ResponseEntity uploadArray(@PathVariable String folder, @RequestPart MultipartFile[] files, @RequestPart UploadMetadata metadata) {
            List<UploadInfo> response = new ArrayList<>();
            for (MultipartFile file : files) {
                response.add(new UploadInfo(1, file.getSize(), folder + "/" + file.getOriginalFilename()));
            }
            return ResponseEntity.ok(response);
        }

    }

}

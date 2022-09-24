package org.nas.hystrix.rest.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.nas.hystrix.rest.client.annotation.EnableHystrixRestClient;
import org.nas.hystrix.rest.client.config.HystrixRestClientConfig;
import org.nas.hystrix.rest.client.service.TestClientNoFallbackFileService;
import org.nas.hystrix.rest.client.util.SetupTests;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.MultipartAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

/**
 * RestClientFileIT.
 *
 * @author Nassim MOUALEK
 */
@ExtendWith({SetupTests.class, SpringExtension.class})
@SpringBootTest(webEnvironment = DEFINED_PORT, classes = HystrixRestClientFileIT.Application.class)
public class HystrixRestClientFileIT {


    @Autowired
    private TestClientNoFallbackFileService testClientNoFallbackFileService;


    @Test
    public void testUploadMuliPart() throws IOException {

        ClassPathResource classPathResource = new ClassPathResource("files/myfile.txt");
        FileDesc fileDesc = testClientNoFallbackFileService.sendFile(classPathResource.getFile());
        //Then
        assertThat(fileDesc).isNotNull();
        assertThat(fileDesc.getFieldName()).isEqualTo("file");
        assertThat(fileDesc.getFileName()).isEqualTo("myfile.txt");
        assertThat(fileDesc.getSize()).isEqualTo(12L);
        assertThat(fileDesc.getParam()).isNull();

    }


    @Test
    public void testUploadMuliPartWithFileNamedToto() throws IOException {

        ClassPathResource classPathResource = new ClassPathResource("files/myfile.txt");
        FileDesc fileDesc = testClientNoFallbackFileService.sendFile2(classPathResource.getFile());
        //Then
        assertThat(fileDesc).isNotNull();
        assertThat(fileDesc.getFieldName()).isEqualTo("toto");
        assertThat(fileDesc.getFileName()).isEqualTo("myfile.txt");
        assertThat(fileDesc.getSize()).isEqualTo(12L);
        assertThat(fileDesc.getParam()).isNull();

    }

    @Test
    public void testUploadMuliPartWithPathParam() throws IOException {

        ClassPathResource classPathResource = new ClassPathResource("files/myfile.txt");
        FileDesc fileDesc = testClientNoFallbackFileService.sendFileWithParam(classPathResource.getFile(), "sfr");
        //Then
        assertThat(fileDesc).isNotNull();
        assertThat(fileDesc.getFieldName()).isEqualTo("file");
        assertThat(fileDesc.getFileName()).isEqualTo("myfile.txt");
        assertThat(fileDesc.getSize()).isEqualTo(12L);
        assertThat(fileDesc.getParam()).isEqualTo("sfr");
    }


    /**
     * Application stater.
     */
    @SpringBootApplication
    @RestController
    @ComponentScan(basePackageClasses = {HystrixRestClientConfig.class})
    @EnableHystrixRestClient(basePackages = {"org.nas.hystrix.rest.client.service"}, clients = {TestClientNoFallbackFileService.class})
    @Import(value = MultipartAutoConfiguration.class)
    protected static class Application {

        @PostMapping(value = "/uploadFile")
        public FileDesc uploadFile(@RequestPart MultipartFile file) {
            return new FileDesc(file);
        }

        @PostMapping(value = "/uploadFileWithFileNamedToto")
        public FileDesc uploadFileWithFileNamedToto(@RequestPart("toto") MultipartFile file) {
            return new FileDesc(file);
        }

        @PostMapping(value = "/uploadFile/{param}")
        public FileDesc uploadFile(@RequestPart MultipartFile file, @PathVariable("param") String param) {
            FileDesc fileDesc = new FileDesc(file);
            fileDesc.setParam(param);
            return fileDesc;
        }
    }

    /**
     * FileDesc.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileDesc {
        String fieldName;
        String fileName;
        long size;
        String param;

        public FileDesc(MultipartFile file) {
            this.fieldName = file.getName();
            this.fileName = file.getOriginalFilename();
            this.size = file.getSize();
        }
    }

}

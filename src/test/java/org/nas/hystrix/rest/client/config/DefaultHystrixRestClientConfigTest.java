package org.nas.hystrix.rest.client.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.nas.hystrix.rest.client.spring.HystrixRestClientProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by Nassim MOUALEK on 21/09/2018.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = DefaultHystrixRestClientConfigTest.ApplicationTest.class)
@Import(HystrixRestClientConfig.class)
public class DefaultHystrixRestClientConfigTest {

    @Autowired
    private ObjectMapper defaultObjectMapper;

    @Autowired
    private DefaultFormattingConversionService feignConversionService;

    @Autowired
    private OkHttpClient okHttpClient;

    @Autowired
    private HystrixRestClientProperties restClientProperties;

    @Test
    public void objectMapperTest() {
        assertThat(defaultObjectMapper).isNotNull();
    }

    @Test
    public void conversionServiceTest() {
        assertThat(feignConversionService).isNotNull();
    }

    @Test
    public void okHttpTest() {

        assertThat(okHttpClient).isNotNull();
        assertThat(okHttpClient.interceptors().size()).isEqualTo(1);
    }

    @Test
    public void restClientTest() {

        assertThat(restClientProperties).isNotNull();
        assertThat(restClientProperties.getMaxAttempts()).isZero();

    }

    @SpringBootApplication
    static class ApplicationTest {
    }


}

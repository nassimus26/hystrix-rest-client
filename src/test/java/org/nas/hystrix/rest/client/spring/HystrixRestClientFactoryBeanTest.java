package org.nas.hystrix.rest.client.spring;

import feign.codec.StringDecoder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.nas.hystrix.rest.client.TestClient;
import org.nas.hystrix.rest.client.TestClientFallback;
import org.nas.hystrix.rest.client.config.HystrixRestClientConfig;
import org.nas.hystrix.rest.client.feign.HystrixRestClientEncoder;
import org.nas.hystrix.rest.client.feign.decoders.ErrorResponseDecoder;
import org.nas.hystrix.rest.client.feign.decoders.HystrixRestClientDecoder;
import org.nas.hystrix.rest.client.hystrix.HystrixRestClientFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by Nassim MOUALEK on 22/09/2018.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = HystrixRestClientFactoryBeanTest.ApplicationTest.class)
@Import(HystrixRestClientConfig.class)
public class HystrixRestClientFactoryBeanTest {

    @Autowired
    private AutowireCapableBeanFactory autowireCapableBeanFactory;
    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void testGetObject() throws Exception {
        //Given
        HystrixRestClientFactoryBean feignClientFactoryBean = new HystrixRestClientFactoryBean();
        feignClientFactoryBean.setType(TestClient.class);
        feignClientFactoryBean.setName("testName");
        feignClientFactoryBean.setUrl("test.url");
        feignClientFactoryBean.setHystrixTimeoutMilliseconds(12);
        feignClientFactoryBean.setConnectTimeoutMilliseconds(11);
        feignClientFactoryBean.setReadTimeoutMilliseconds(10);
        feignClientFactoryBean.setSemaphoreMaxConcurrentRequests(9);
        feignClientFactoryBean.setFallback(new TestClientFallback());
        feignClientFactoryBean.setAutowireCapableBeanFactory(autowireCapableBeanFactory);
        feignClientFactoryBean.setApplicationContext(applicationContext);
        feignClientFactoryBean.setFeignEncoder(HystrixRestClientEncoder.JACKSON_ENCODER);
        feignClientFactoryBean.setFeignDecoder(HystrixRestClientDecoder.JACKSON_DECODER);
        feignClientFactoryBean.setErrorDecoder(new ErrorResponseDecoder());
        feignClientFactoryBean.setCustomDecoder(new StringDecoder());

        //When
        Object fallback = feignClientFactoryBean.getFallback();
        Object object = feignClientFactoryBean.getObject();

        //Then
        assertThat(fallback).isNotNull();
        assertThat(object).isNotNull();
        assertThat(fallback).isExactlyInstanceOf(TestClientFallback.class);
        assertThat(feignClientFactoryBean.getUrl()).isEqualTo("http://test.url");
        assertThat(feignClientFactoryBean.getHystrixTimeoutMilliseconds()).isEqualTo(12);
        assertThat(feignClientFactoryBean.getConnectTimeoutMilliseconds()).isEqualTo(11);
        assertThat(feignClientFactoryBean.getReadTimeoutMilliseconds()).isEqualTo(10);
        assertThat(feignClientFactoryBean.getSemaphoreMaxConcurrentRequests()).isEqualTo(9);
        assertThat(feignClientFactoryBean.getFallback()).isNotNull();
        assertThat(feignClientFactoryBean.getApplicationContext()).isNotNull();
        Assertions.assertThat(feignClientFactoryBean.getFeignEncoder()).isNotNull();
        Assertions.assertThat(feignClientFactoryBean.getFeignDecoder()).isNotNull();
        assertThat(feignClientFactoryBean.getErrorDecoder()).isNotNull();
        assertThat(feignClientFactoryBean.getCustomDecoder()).isNotNull();
        assertThat(feignClientFactoryBean.getName()).isNotNull();
        assertThat(feignClientFactoryBean.getType()).isNotNull();
        assertThat(feignClientFactoryBean.isUseThreads()).isFalse();
        assertThat(feignClientFactoryBean.getAutowireCapableBeanFactory()).isNotNull();
    }

    @SpringBootApplication
    static class ApplicationTest {
    }
}

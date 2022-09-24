package org.nas.hystrix.rest.client.hystrix;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Request;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.codec.ErrorDecoder;
import feign.codec.StringDecoder;
import feign.form.FormEncoder;
import feign.hystrix.HystrixFeign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.nas.hystrix.rest.client.feign.HystrixRestClientEncoder;
import org.nas.hystrix.rest.client.feign.HystrixRestClientRetryer;
import org.nas.hystrix.rest.client.feign.decoders.HystrixRestClientDecoder;
import org.nas.hystrix.rest.client.feign.decoders.HystrixRestClientFormEncoder;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;

import static org.springframework.util.StringUtils.hasText;

/**
 * Created by Nassim MOUALEK on 17/09/2018.
 */
@Getter
@Setter
@Slf4j
public class HystrixRestClientFactoryBean implements FactoryBean<Object>, ApplicationContextAware, InitializingBean {

    private Class<?> type;
    private String name;
    private Object fallback;
    private String url;
    private int hystrixTimeoutMilliseconds;
    private int connectTimeoutMilliseconds;
    private int readTimeoutMilliseconds;
    private int semaphoreMaxConcurrentRequests;
    private boolean useThreads;
    private AutowireCapableBeanFactory autowireCapableBeanFactory;
    private ApplicationContext applicationContext;
    private HystrixRestClientEncoder feignEncoder;
    private HystrixRestClientDecoder feignDecoder;
    private ErrorDecoder errorDecoder;
    private Decoder customDecoder;

    @Override
    @SuppressWarnings("unchecked")
    public Object getObject() throws Exception {
        verifyTimeouts();

        if (hasText(this.url) && !this.url.startsWith("http")) {
            this.url = "http://" + this.url;
        }
        log.info("Create Feign client name : {} , fallback : {} , type : {} , url : {} , semaphoreMaxConcurrentRequests : {} ", name,
                fallback, type, url, semaphoreMaxConcurrentRequests);

        // Create SetterFactory from annotation value
        CommandSetterFactory feignSetterFactory = new CommandSetterFactory(this.name, this.hystrixTimeoutMilliseconds, this.useThreads,
                this.semaphoreMaxConcurrentRequests);

        // Get OkHttp bean
        OkHttpClient okHttpClient = applicationContext.getBean(OkHttpClient.class);

        //Create Feign and hystrixCommand fallback
        HystrixFeign.Builder builder = HystrixFeign.builder().setterFactory(feignSetterFactory)
                .encoder(getEncoder())
                .decoder(getDecoder())
                .options(new Request.Options(connectTimeoutMilliseconds, readTimeoutMilliseconds))
                .client(new feign.okhttp.OkHttpClient(okHttpClient));

        HystrixRestClientRetryer restClientRetryer = applicationContext.getBean(HystrixRestClientRetryer.class);
        builder.retryer(restClientRetryer);
        //Decoder
        builder.errorDecoder(errorDecoder);
        if (fallback == null) {
            return builder.target(type, url);
        }
        // Create bean Fallback
        Object beanFallback = autowireCapableBeanFactory.createBean(fallback.getClass());
        return builder.target(type, url, new HystrixRestClientFallbackFactory(beanFallback));
    }

    /**
     * Verify that the hystrix timeout is higher than the connect and read timeouts.
     * Else throws an IllegalArgumentException.
     */
    private void verifyTimeouts() {
        if (hystrixTimeoutMilliseconds < connectTimeoutMilliseconds) {
            throw new IllegalArgumentException("Hystrix timeout is lower than connect timeout (" + hystrixTimeoutMilliseconds + " vs "
                    + connectTimeoutMilliseconds + ").");
        } else if (hystrixTimeoutMilliseconds < readTimeoutMilliseconds) {
            throw new IllegalArgumentException("Hystrix timeout is lower than read timeout (" + hystrixTimeoutMilliseconds + " vs "
                    + readTimeoutMilliseconds + ").");
        }
    }

    /**
     * Get request encoder.
     *
     * @return Encoder.
     */
    private Encoder getEncoder() {

        ObjectMapper defaultObjectMapper = applicationContext.getBean(ObjectMapper.class);
        JacksonEncoder jacksonEncoder = new JacksonEncoder(defaultObjectMapper);

        switch (feignEncoder) {
            case FILE_ENCODER:
                return new FormEncoder(jacksonEncoder);
            case MULTIPART_ENCODER:
                return new HystrixRestClientFormEncoder(jacksonEncoder);
            default:
                return jacksonEncoder;
        }
    }

    /**
     * Get response decoder.
     *
     * @return Decoder.
     */
    private Decoder getDecoder() {
        if (HystrixRestClientDecoder.STRING_DECODER.equals(feignDecoder)) {
            return new StringDecoder();
        } else if (HystrixRestClientDecoder.CUSTOM_DECODER.equals(feignDecoder)) {
            return customDecoder;
        }
        //Get Object mapper
        ObjectMapper defaultObjectMapper = applicationContext.getBean(ObjectMapper.class);
        return new JacksonDecoder(defaultObjectMapper);
    }

    @Override
    public Class<?> getObjectType() {
        return this.type;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.hasText(this.name, "Name must be set");
    }
}

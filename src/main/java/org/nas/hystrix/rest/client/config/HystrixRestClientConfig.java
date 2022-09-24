package org.nas.hystrix.rest.client.config;


import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.nas.hystrix.rest.client.feign.HystrixRestClientRetryer;
import org.nas.hystrix.rest.client.okhttp3.HttpLoggerService;
import org.nas.hystrix.rest.client.okhttp3.LoggingInterceptor;
import org.nas.hystrix.rest.client.spring.HystrixRestClientProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.util.CollectionUtils;

import java.util.Collection;

/**
 * Created by Nassim MOUALEK on 17/09/2018.
 */
@Configuration
@EnableConfigurationProperties(value = {HystrixRestClientProperties.class})
public class HystrixRestClientConfig {

    @Bean
    @ConditionalOnMissingBean(ConversionService.class)
    public DefaultFormattingConversionService feignConversionService() {
        return new DefaultFormattingConversionService();
    }

    @Bean
    public OkHttpClient okHttpClient(@Autowired(required = false) Collection<Interceptor> interceptors) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder().retryOnConnectionFailure(false);
        if (!CollectionUtils.isEmpty(interceptors)) {
            interceptors.forEach(builder::addInterceptor);
        }
        return builder.build();
    }

    @Bean
    public HystrixRestClientRetryer restClientRepeater(HystrixRestClientProperties restClientProperties) {
        return new HystrixRestClientRetryer(restClientProperties.getMaxAttempts());
    }

    @Bean
    @ConditionalOnExpression("'${hystrix-rest-client.okhttp.logging-level:NONE}'!='NONE'")
    public HttpLoggingInterceptor httpLoggingInterceptor(HystrixRestClientProperties restClientProperties) {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(restClientProperties.getOkhttp().getLoggingLevel());
        return loggingInterceptor;
    }

    @Bean
    @ConditionalOnProperty(prefix = "hystrix-rest-client.okhttp", name = "structured-log", havingValue = "true", matchIfMissing = true)
    public LoggingInterceptor loggingInterceptor() {
        return new LoggingInterceptor(new HttpLoggerService());
    }

}

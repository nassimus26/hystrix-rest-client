package org.nas.hystrix.rest.client.hystrix;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties.ExecutionIsolationStrategy;
import com.netflix.hystrix.HystrixCommandProperties.Setter;
import feign.Target;
import feign.hystrix.SetterFactory;
import lombok.AllArgsConstructor;

import java.lang.reflect.Method;

import static com.netflix.hystrix.HystrixCommand.Setter.withGroupKey;
import static com.netflix.hystrix.HystrixCommandProperties.ExecutionIsolationStrategy.SEMAPHORE;
import static com.netflix.hystrix.HystrixCommandProperties.ExecutionIsolationStrategy.THREAD;
import static com.netflix.hystrix.HystrixCommandProperties.Setter;

/**
 * Created by Nassim MOUALEK on 17/09/2018.
 */
@AllArgsConstructor
public class CommandSetterFactory implements SetterFactory {

    private String name;
    private int hystrixTimeoutMilliseconds;
    private boolean useThread;
    private int semaphoreMaxConcurrentRequests;

    @Override
    public HystrixCommand.Setter create(Target<?> target, Method method) {

        ExecutionIsolationStrategy strategy = useThread ? THREAD : SEMAPHORE;
        Setter properties = Setter();
        //When use semaphore strategy
        if (!useThread) {
            properties.withExecutionIsolationSemaphoreMaxConcurrentRequests(semaphoreMaxConcurrentRequests)
                    .withFallbackIsolationSemaphoreMaxConcurrentRequests(semaphoreMaxConcurrentRequests);
        }

        properties.withExecutionTimeoutEnabled(hystrixTimeoutMilliseconds != 0)
                .withExecutionTimeoutInMilliseconds(hystrixTimeoutMilliseconds)
                .withExecutionIsolationStrategy(strategy);

        return withGroupKey(HystrixCommandGroupKey.Factory.asKey(name))
                .andCommandKey(HystrixCommandKey.Factory.asKey(name))
                .andCommandPropertiesDefaults(properties);
    }

}

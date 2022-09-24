/*
 * Copyright 2013-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.nas.hystrix.rest.client.annotation;

import org.nas.hystrix.rest.client.config.HystrixRestClientConfig;
import org.nas.hystrix.rest.client.hystrix.HystrixRestClientsRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Created by Nassim MOUALEK on 17/09/2018.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import({HystrixRestClientConfig.class, HystrixRestClientsRegistrar.class})
public @interface EnableHystrixRestClient {

    /**
     * List of interfaces annotated with @HystrixRestClient. If not empty, disables classpath scanning.
     *
     * @return the array of clients
     */
    Class<?>[] clients() default {};


    /**
     * Base packages to scan for annotated components.
     *
     * @return the array of 'basePackages'.
     */
    String[] basePackages() default {};

}

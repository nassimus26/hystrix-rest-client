package org.nas.hystrix.rest.client.util;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SpringBootAppRunner {
    private static final String[] DEFAULT_PROFILES = new String[]{"test"};

    private SpringBootAppRunner() {
        throw new IllegalAccessError("Utility class");
    }

    /**
     * Run SpringBoot application with specified parameters.
     *
     * @param clazz Main class to launch
     * @return Spring application Context
     */
    public static ConfigurableApplicationContext run(Class clazz) {
        return runWithProfilesAndEnvs(clazz, DEFAULT_PROFILES, Collections.emptyMap());
    }

    /**
     * Run SpringBoot application with specified parameters.
     *
     * @param clazz    Main class to launch
     * @param profiles Profiles used to launch application
     * @return Spring application Context
     */
    public static ConfigurableApplicationContext runWithProfiles(Class clazz, String... profiles) {
        return runWithProfilesAndEnvs(clazz, profiles, Collections.emptyMap());
    }

    /**
     * Run SpringBoot application with specified parameters.
     *
     * @param clazz Main class to launch
     * @param envs  Default properties used to launch application
     * @return Spring application Context
     */
    public static ConfigurableApplicationContext runWithEnvs(Class clazz, Map<String, Object> envs) {
        return runWithProfilesAndEnvs(clazz, DEFAULT_PROFILES, envs);
    }

    /**
     * Run SpringBoot application with specified parameters.
     *
     * @param clazz Main class to launch
     * @param envs  Default properties used to launch application
     * @return Spring application Context
     */
    public static ConfigurableApplicationContext runWithEnvs(Class clazz, String... envs) {
        return runWithProfilesAndEnvs(clazz, DEFAULT_PROFILES, envs);
    }

    /**
     * Run SpringBoot application with specified parameters.
     *
     * @param clazz    Main class to launch
     * @param profiles Profiles used to launch application
     * @param envs     Default properties used to launch application
     * @return Spring application Context
     */
    public static ConfigurableApplicationContext runWithProfilesAndEnvs(Class clazz, String[] profiles, String... envs) {
        Assert.isTrue(envs.length % 2 == 0);
        Map<String, Object> var = new HashMap<>();
        for (int i = 0; i < envs.length; i += 2) {
            var.put(envs[i], envs[i + 1]);
        }
        return runWithProfilesAndEnvs(clazz, profiles, var);
    }

    /**
     * Run SpringBoot application with specified parameters.
     *
     * @param clazz    Main class to launch
     * @param profiles Profiles used to launch application
     * @param envs     Default properties used to launch application
     * @return Spring application Context
     */
    public static ConfigurableApplicationContext runWithProfilesAndEnvs(Class clazz, String[] profiles, Map<String, Object> envs) {
        SpringApplication application = new SpringApplication(clazz);
        application.setAdditionalProfiles(profiles);
        application.setDefaultProperties(envs);
        return application.run();
    }

}

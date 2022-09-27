package org.nas.hystrix.rest.client.hystrix;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nas.hystrix.rest.client.annotation.EnableHystrixRestClient;
import org.nas.hystrix.rest.client.annotation.HystrixRestClient;
import org.nas.hystrix.rest.client.spring.AllTypeFilter;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.filter.AbstractClassTestingTypeFilter;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.springframework.beans.factory.support.AbstractBeanDefinition.AUTOWIRE_BY_TYPE;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.genericBeanDefinition;
import static org.springframework.beans.factory.support.BeanDefinitionReaderUtils.registerBeanDefinition;
import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;
import static org.springframework.util.ClassUtils.getPackageName;
import static org.springframework.util.StringUtils.hasText;

/**
 * Created by Nassim MOUALEK on 17/09/2018.
 */
@Slf4j
@NoArgsConstructor
public final class HystrixRestClientsRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware, BeanClassLoaderAware {

    private static final String HTTP = "http://";
    private static final String FALLBACK = "fallbackClass";
    private static final String PROPERTY_NAME_CUSTOM_DECODER = "customDecoder";
    private static final String DEFAULT_PROPERTY_PREFIX = "hystrix-rest-client.";
    private ResourceLoader resourceLoader;
    private ClassLoader classLoader;
    @LocalServerPort
    private int port;

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata,
                                        BeanDefinitionRegistry registry) {
        registerFeignClients(metadata, registry);
    }

    public void registerFeignClients(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        ClassPathScanningCandidateComponentProvider scanner = getScanner();
        scanner.setResourceLoader(this.resourceLoader);

        Set<String> basePackages = new HashSet<>();
        Map<String, Object> attrs = metadata.getAnnotationAttributes(EnableHystrixRestClient.class.getName());
        AnnotationTypeFilter annotationTypeFilter = new AnnotationTypeFilter(HystrixRestClient.class);

        final Class<?>[] clients = attrs == null ? null : (Class<?>[]) attrs.get("clients");
        final String[] packages = attrs == null ? null : (String[]) attrs.get("basePackages");
        if (packages != null) {
            scanner.addIncludeFilter(annotationTypeFilter);
            basePackages = getBasePackages(metadata);
        }
        if (clients != null && clients.length != 0) {
            final Set<String> clientClasses = new HashSet<>();
            basePackages = new HashSet<>();
            for (Class<?> clazz : clients) {
                basePackages.add(getPackageName(clazz));
                clientClasses.add(clazz.getCanonicalName());
            }
            AbstractClassTestingTypeFilter filter = new AbstractClassTestingTypeFilter() {
                @Override
                protected boolean match(ClassMetadata metadata) {
                    String cleaned = metadata.getClassName().replaceAll("\\$", ".");
                    return clientClasses.contains(cleaned);
                }
            };
            scanner.addIncludeFilter(new AllTypeFilter(asList(filter, annotationTypeFilter)));
        }
        for (String basePackage : basePackages) {
            Set<BeanDefinition> candidateComponents = scanner.findCandidateComponents(basePackage);
            for (BeanDefinition candidateComponent : candidateComponents) {
                if (candidateComponent instanceof AnnotatedBeanDefinition) {
                    // verify annotated class is an interface
                    AnnotatedBeanDefinition beanDefinition = (AnnotatedBeanDefinition) candidateComponent;
                    AnnotationMetadata annotationMetadata = beanDefinition.getMetadata();
                    isTrue(annotationMetadata.isInterface(),
                            "@HystrixRestClient can only be specified on an interface");

                    Map<String, Object> attributes = annotationMetadata.getAnnotationAttributes(HystrixRestClient.class.getCanonicalName());
                    registerFeignClient(registry, annotationMetadata, attributes);
                }
            }
        }
    }

    private void registerFeignClient(BeanDefinitionRegistry registry,
                                     AnnotationMetadata annotationMetadata, Map<String, Object> attributes) {
        String className = annotationMetadata.getClassName();
        BeanDefinitionBuilder definition = genericBeanDefinition(HystrixRestClientFactoryBean.class);
        String name = getName(attributes);

        definition.addPropertyValue("url", getUrl(attributes, name));
        definition.addPropertyValue("name", name);
        definition.addPropertyValue("type", className);
        definition.addPropertyValue("hystrixTimeoutMilliseconds", resolveProperty(DEFAULT_PROPERTY_PREFIX + name +
                        ".hystrixTimeoutMilliseconds",
                String.class, String.valueOf(attributes.get("hystrixTimeoutMilliseconds"))));
        definition.addPropertyValue("connectTimeoutMilliseconds", resolveProperty(DEFAULT_PROPERTY_PREFIX + name +
                        ".connectTimeoutMilliseconds",
                String.class, String.valueOf(attributes.get("connectTimeoutMilliseconds"))));
        definition.addPropertyValue("readTimeoutMilliseconds", resolveProperty(DEFAULT_PROPERTY_PREFIX + name +
                        ".readTimeoutMilliseconds",
                String.class, String.valueOf(attributes.get("readTimeoutMilliseconds"))));
        definition.addPropertyValue("semaphoreMaxConcurrentRequests", attributes.get("semaphoreMaxConcurrentRequests"));
        definition.addPropertyValue("useThreads", attributes.get("useThreads"));
        definition.addPropertyValue("feignEncoder", attributes.get("encoder"));
        definition.addPropertyValue("feignDecoder", attributes.get("decoder"));
        definition.addPropertyValue(PROPERTY_NAME_CUSTOM_DECODER, attributes.get(PROPERTY_NAME_CUSTOM_DECODER));
        definition.setAutowireMode(AUTOWIRE_BY_TYPE);
        String fallbackBeanName = (String) attributes.get("fallback");
        if (!fallbackBeanName.isEmpty())
            definition.addPropertyReference("fallback", fallbackBeanName);
        else {
            Class<?> clazzFallback = getFallBackClass(className, attributes);
            try {
                if (clazzFallback != null) {
                    definition.addPropertyValue(FALLBACK, clazzFallback.getDeclaredConstructor().newInstance());
                }
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                throw new BeanInitializationException("Cannot register feign client", e);
            }
        }
        try {
            Class<?> errorDecoderClass = (Class) attributes.get("errorDecoder");
            definition.addPropertyValue("errorDecoder", errorDecoderClass.getDeclaredConstructor().newInstance());

        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new BeanInitializationException("Cannot create error decoder", e);
        }
        try {
            Class<?> customDecoderClass = (Class) attributes.get(PROPERTY_NAME_CUSTOM_DECODER);
            definition.addPropertyValue(PROPERTY_NAME_CUSTOM_DECODER, customDecoderClass.getDeclaredConstructor().newInstance());

        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new BeanInitializationException("Cannot create error customDecoderClass", e);
        }

        String alias = name + "FeignClient";
        AbstractBeanDefinition beanDefinition = definition.getBeanDefinition();
        beanDefinition.setPrimary(true);
        BeanDefinitionHolder holder = new BeanDefinitionHolder(beanDefinition, className, new String[]{alias});
        registerBeanDefinition(holder, registry);
    }

    /**
     * Generate class from source class name.
     *
     * @param className  source class name.
     * @param attributes source class attributes.
     * @return generated class.
     */
    @SuppressWarnings("squid:S1166")
    private Class<?> getFallBackClass(String className, Map<String, Object> attributes) {
        Class<?> clazzFallback = null;

        try {
            log.info("Fallback {} ", attributes.get(FALLBACK));
            Class classFallback = (Class) attributes.get(FALLBACK);
            if (classFallback.equals(void.class)) {
                clazzFallback = Class.forName(className + "Fallback");
            } else {
                clazzFallback = Class.forName(classFallback.getName());
            }

        } catch (ClassNotFoundException e) {
            log.warn(e.getMessage());
        }
        return clazzFallback;
    }

    String getName(Map<String, Object> attributes) {
        String name = (String) attributes.get("name");
        if (!hasText(name)) {
            name = (String) attributes.get("value");
        }
        name = resolve(name);
        notNull(name, "Service name must be not null");
        return name;

    }

    private String resolve(String value) {
        return ((ConfigurableApplicationContext) this.resourceLoader).getEnvironment().resolvePlaceholders(value);
    }

    private <T> T resolveProperty(String propertyName, Class<T> targetType, T defaultPropertyValue) {
        return getPropertyValue(propertyName, targetType)
                .map(property -> {
                    // Properties have precedence over parameters
                    log.debug("Using resolved value of {} for property: {}.", property, propertyName);
                    return property;
                }).orElseGet(() -> {
                    log.debug("Using default value of {} for property: {}.", defaultPropertyValue, propertyName);
                    return defaultPropertyValue;
                });
    }

    private <T> Optional<T> getPropertyValue(String property, Class<T> targetType) {
        return Optional.ofNullable(((ConfigurableApplicationContext) this.resourceLoader).getEnvironment().getProperty(property, targetType));
    }

    private String getUrl(Map<String, Object> attributes, String name) {
        String defaultUrlValue = (String) attributes.get("url");
        if (!hasText(defaultUrlValue) || !defaultUrlValue.startsWith("${")) {
            defaultUrlValue = "${hystrix-rest-client." + name + ".url}";
        }

        String url = resolve(defaultUrlValue);
        if (hasText(url) && !(url.startsWith("#{") && url.endsWith("}"))) {
            if (!url.startsWith(HTTP) && !url.startsWith("https://")) {
                url = HTTP + url;
            }
            try {
                new URL(url);
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException(url + " is malformed", e);
            }
        }
        return url;
    }

    protected ClassPathScanningCandidateComponentProvider getScanner() {
        return new ClassPathScanningCandidateComponentProvider(false) {

            @Override
            protected boolean isCandidateComponent(
                    AnnotatedBeanDefinition beanDefinition) {

                if (beanDefinition.getMetadata().isIndependent()) {
                    if (beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().getInterfaceNames().length == 1
                            && (beanDefinition.getMetadata() instanceof Annotation)) {
                        try {
                            Class<?> target = ClassUtils.forName(beanDefinition.getMetadata().getClassName(), HystrixRestClientsRegistrar.this.classLoader);
                            return !target.isAnnotation();
                        } catch (Exception ex) {
                            this.logger.error("Could not load target class: " + beanDefinition.getMetadata().getClassName(), ex);

                        }
                    }
                    return true;
                }
                return false;
            }
        };
    }

    protected Set<String> getBasePackages(AnnotationMetadata importingClassMetadata) {
        Map<String, Object> attributes = importingClassMetadata.getAnnotationAttributes(EnableHystrixRestClient.class.getCanonicalName());

        Set<String> basePackages = new HashSet<>();

        for (String pkg : (String[]) attributes.get("basePackages")) {
            if (hasText(pkg)) {
                basePackages.add(pkg);
            }
        }

        if (basePackages.isEmpty()) {
            basePackages.add(getPackageName(importingClassMetadata.getClassName()));
        }
        return basePackages;
    }

}

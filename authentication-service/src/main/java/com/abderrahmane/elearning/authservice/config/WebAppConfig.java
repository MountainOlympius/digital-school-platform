package com.abderrahmane.elearning.authservice.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.abderrahmane.elearning.authservice.converters.JsonMapConverter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.orm.hibernate5.HibernateExceptionTranslator;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "com.abderrahmane.elearning.authservice")
@PropertySources(@PropertySource("classpath:jdbc.properties"))
public class WebAppConfig implements WebMvcConfigurer {
    @Autowired
    Environment environment;

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.addBasenames("messages");
        return messageSource;
    }

    @Bean
    EntityManager entityManager() {
        // EntityManager entityManager = entityManagerFactory().createEntityManager();
        return entityManagerFactory().createEntityManager();
    }

    @Bean
    public EntityManagerFactory entityManagerFactory() {
        Map<String, String> jpaProperties = new HashMap<String, String>();
        jpaProperties.put("javax.persistence.jdbc.driver", environment.getProperty("jdbc.driver"));
        jpaProperties.put("javax.persistence.jdbc.url", environment.getProperty("jdbc.url"));
        jpaProperties.put("javax.persistence.jdbc.user", environment.getProperty("jdbc.user"));
        jpaProperties.put("javax.persistence.jdbc.password", environment.getProperty("jdbc.password"));

        return Persistence.createEntityManagerFactory("main-unit", jpaProperties);
    }

    /* Configuring Message Converters */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(new JsonMapConverter());
        converters.add(new StringHttpMessageConverter());
    }

    /* Adding persistence exception translator */
    @Bean
    public PersistenceExceptionTranslationPostProcessor persistenceExceptionTranslationPostProcessor () {
        return new PersistenceExceptionTranslationPostProcessor();
    }

    @Bean
    public PersistenceExceptionTranslator persistenceExceptionTranslator () {
        return new HibernateExceptionTranslator();
    }
}

package com.example.payments.config;

import io.kurrent.dbclient.KurrentDBClient;
import io.kurrent.dbclient.KurrentDBClientSettings;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(KurrentDbProperties.class)
public class KurrentDbConfig {
    @Bean
    KurrentDBClient kurrentDBClient(KurrentDbProperties properties) throws Exception {
        KurrentDBClientSettings settings = KurrentDBClientSettings.builder()
                .addHost("localhost", 2113)
                .tls(false)
                .buildConnectionSettings();
        return KurrentDBClient.create(settings);
    }
}

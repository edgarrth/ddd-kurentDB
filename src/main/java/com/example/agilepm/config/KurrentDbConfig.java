package com.example.agilepm.config;

import io.kurrent.dbclient.KurrentDBClient;
import io.kurrent.dbclient.KurrentDBClientSettings;
import io.kurrent.dbclient.KurrentDBConnectionString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KurrentDbConfig {
    @Bean
    KurrentDBClient kurrentDBClient(@Value("${kurrentdb.connection-string}") String connectionString) {
        KurrentDBClientSettings settings = KurrentDBConnectionString.parseOrThrow(connectionString);
        return KurrentDBClient.create(settings);
    }
}

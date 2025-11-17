package com.villaggiogirotto.split.villagiosplit.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
public class PagarmeWebClientConfig {


    @Value("${pagarme.api-key}")
    private String apiKey;

    @Value("${pagarme.base-url}")
    private String baseUrl;

    @Bean
    public WebClient pagarmeWebClient() {

        String basicAuth = Base64.getEncoder()
                .encodeToString((apiKey + ":").getBytes(StandardCharsets.UTF_8));

        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Basic " + basicAuth)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}

package com.inn.config;

import com.siot.IamportRestClient.IamportClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IamportConfig {

    @Bean
    public IamportClient iamportClient(
            @Value("${portone.api-key}") String apiKey,
            @Value("${portone.api-secret}") String apiSecret
    ) {
        return new IamportClient(apiKey, apiSecret);
    }
}

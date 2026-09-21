package com.PrzeBarCore.Laboratorymanagementsystem.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
@Profile("aws")
public class AwsSqsConfiguration {
    @Bean
    public SqsClient sqsClient(){
        return SqsClient.create();
    }
}

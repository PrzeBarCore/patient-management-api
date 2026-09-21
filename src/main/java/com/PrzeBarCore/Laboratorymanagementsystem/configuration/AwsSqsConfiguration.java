package com.PrzeBarCore.Laboratorymanagementsystem.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
public class AwsSqsConfiguration {
    @Bean
    public SqsClient sqsClient(){
        return SqsClient.create();
    }
}

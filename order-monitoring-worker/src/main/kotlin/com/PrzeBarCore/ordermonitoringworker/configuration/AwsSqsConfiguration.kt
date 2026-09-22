package com.PrzeBarCore.ordermonitoringworker.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import software.amazon.awssdk.services.sqs.SqsClient

@Profile("aws")
@Configuration
class AwsSqsConfiguration {
    @Bean
    fun sqsClient(): SqsClient = SqsClient.create()
}
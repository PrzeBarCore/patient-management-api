package com.PrzeBarCore.ordermonitoringworker

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class OrderMonitoringWorkerApplication

fun main(args: Array<String>) {
	runApplication<OrderMonitoringWorkerApplication>(*args)
}

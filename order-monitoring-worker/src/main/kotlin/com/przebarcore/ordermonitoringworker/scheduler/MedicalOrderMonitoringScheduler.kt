package com.przebarcore.ordermonitoringworker.scheduler

import com.przebarcore.ordermonitoringworker.service.MedicalOrderMonitoringService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(
name = ["monitoring.scheduler-enabled"],
havingValue = "true",
matchIfMissing = true
)
class MedicalOrderMonitoringScheduler(
    private val medicalOrderMonitoringService : MedicalOrderMonitoringService) {

    @Scheduled(fixedDelayString  = "\${monitoring.scheduler-fixed-delay}")
    fun monitorOrdersDeadline(){
        medicalOrderMonitoringService.reviewOrdersDeadline()
    }
}
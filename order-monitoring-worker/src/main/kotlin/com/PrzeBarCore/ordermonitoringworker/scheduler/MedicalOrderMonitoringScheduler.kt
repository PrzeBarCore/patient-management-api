package com.przebarcore.ordermonitoringworker.scheduler

import com.przebarcore.ordermonitoringworker.service.MedicalOrderMonitoringService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class MedicalOrderMonitoringScheduler(
    private val medicalOrderMonitoringService : MedicalOrderMonitoringService) {

    @Scheduled(fixedDelayString  = "\${monitoring.scheduler-fixed-delay}")
    fun monitorOrdersDeadline(){
        medicalOrderMonitoringService.reviewOrdersDeadline()
    }
}
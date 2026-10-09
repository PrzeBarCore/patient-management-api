package com.przebarcore.ordermonitoringworker.handler

import com.przebarcore.ordermonitoringworker.dto.MedicalOrderStatusChangedEvent
import com.przebarcore.ordermonitoringworker.dto.PublishedEvent
import com.przebarcore.ordermonitoringworker.global.enums.EventType
import com.przebarcore.ordermonitoringworker.service.MedicalOrderMonitoringService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper

@Component
class MedicalOrderStatusChangedHandler(private val objectMapper: ObjectMapper,
                                       private val service : MedicalOrderMonitoringService)
    : EventHandler {

    override val eventType = EventType.MEDICAL_ORDER_STATUS_CHANGED
    override fun handle(receivedEvent: PublishedEvent) {
        val eventDetails = objectMapper.treeToValue(receivedEvent.payload,MedicalOrderStatusChangedEvent::class.java)
        log.info("Medical order status changed event. Order Id = {}, Old status = {}, New status = {}",
            eventDetails.orderId,
            eventDetails.oldStatus,
            eventDetails.newStatus)
        service.processOrderStatusChanged(eventDetails)
    }

    companion object {
        private val log =
            LoggerFactory.getLogger(MedicalOrderStatusChangedHandler::class.java)
    }
}
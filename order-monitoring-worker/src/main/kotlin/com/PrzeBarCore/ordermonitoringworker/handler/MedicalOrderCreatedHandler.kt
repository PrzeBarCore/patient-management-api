package com.przebarcore.ordermonitoringworker.handler

import com.przebarcore.ordermonitoringworker.dto.MedicalOrderCreatedEvent
import com.przebarcore.ordermonitoringworker.dto.PublishedEvent
import com.przebarcore.ordermonitoringworker.global.enums.EventType
import com.przebarcore.ordermonitoringworker.service.MedicalOrderMonitoringService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper

@Component
class MedicalOrderCreatedHandler(private val objectMapper: ObjectMapper,
                                       private val service : MedicalOrderMonitoringService)
    : EventHandler {

    override val eventType = EventType.MEDICAL_ORDER_CREATED
    override fun handle(receivedEvent: PublishedEvent) {
        val eventDetails = objectMapper.treeToValue(receivedEvent.payload, MedicalOrderCreatedEvent::class.java)
        log.info("Medical Order was created. Order Id = {}, status = {}",
            eventDetails.orderId,
            eventDetails.status)
        service.processOrderCreated(eventDetails)
    }

    companion object {
        private val log =
            LoggerFactory.getLogger(MedicalOrderCreatedHandler::class.java)
    }
}
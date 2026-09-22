package com.PrzeBarCore.ordermonitoringworker.handler

import com.PrzeBarCore.ordermonitoringworker.dto.MedicalOrderStatusChangedEvent
import com.PrzeBarCore.ordermonitoringworker.dto.PublishedEvent
import com.PrzeBarCore.ordermonitoringworker.global.enums.EventType
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper

@Component
class MedicalOrderStatusChangedHandler(private val objectMapper: ObjectMapper) : EventHandler {

    override val eventType = EventType.MEDICAL_ORDER_STATUS_CHANGED
    override fun handle(receivedEvent: PublishedEvent) {
        val eventDetails = objectMapper.treeToValue(receivedEvent.payload,MedicalOrderStatusChangedEvent::class.java)

        log.info("Medical order status changed event. Order Id = {}, Old status = {}, New status = {}",
            eventDetails.orderId,
            eventDetails.oldStatus,
            eventDetails.newStatus)
    }

    companion object {
        private val log =
            LoggerFactory.getLogger(MedicalOrderStatusChangedHandler::class.java)
    }
}
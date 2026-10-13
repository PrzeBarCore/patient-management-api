package com.przebarcore.notificationservice.inbox

import com.przebarcore.notificationservice.dto.incoming.NotificationRequestedEvent
import com.przebarcore.notificationservice.dto.incoming.RetrievedEvent
import com.przebarcore.notificationservice.global.enums.InboxEventType
import com.przebarcore.notificationservice.repository.NotificationRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.ObjectMapper

@Service
class EventProcessor(
    private val objectMapper: ObjectMapper,
    private val notificationRepository: NotificationRepository) {

    companion object {
        private val logger = LoggerFactory.getLogger(EventProcessor::class.toString())
    }

    @Transactional
    fun process(retrievedEvent: RetrievedEvent){
        if(retrievedEvent.eventType == InboxEventType.NOTIFICATION_REQUESTED){
            val notificationRequest = objectMapper.treeToValue(retrievedEvent.payload, NotificationRequestedEvent::class.java)

            val result = notificationRepository.tryCreateNotification(
                notificationRequest.requestId,
                notificationRequest.referenceType.name,
                notificationRequest.referenceId,
                notificationRequest.notificationType.name,
                notificationRequest.createdAt,
            )

            if (result == 0)
                logger.info("Notification for request {} already exists. Event ignored", notificationRequest.requestId)
            else
                logger.info("Created notification with id: {} ", notificationRequest.requestId)
        } else {
            throw IllegalArgumentException("Unknown event type: ${retrievedEvent.eventType}")
        }
    }
}
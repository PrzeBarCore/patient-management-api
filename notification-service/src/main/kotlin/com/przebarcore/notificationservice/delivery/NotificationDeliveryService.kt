package com.przebarcore.notificationservice.delivery

import com.przebarcore.notificationservice.global.enums.NotificationStatus
import com.przebarcore.notificationservice.repository.NotificationRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class NotificationDeliveryService(
    private val notificationRepository: NotificationRepository,
    private val notificationProcessor: NotificationProcessor) {

    companion object {
        private val logger = LoggerFactory.getLogger(NotificationDeliveryService::class.java)
    }
    fun processPendingNotifications(){
        val notificationIdsToProcess = notificationRepository.findAllRequestIdByStatus(NotificationStatus.PENDING)
        for(notificationId in notificationIdsToProcess){
            try{
                notificationProcessor.processNotification(notificationId)
            } catch (e : Exception){
                logger.error("Error while processing notification: {}",notificationId, e)
            }
        }
    }
}


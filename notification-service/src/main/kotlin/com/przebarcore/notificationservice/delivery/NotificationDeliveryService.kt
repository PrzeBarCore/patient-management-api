package com.przebarcore.notificationservice.delivery

import com.przebarcore.notificationservice.global.enums.NotificationStatus
import com.przebarcore.notificationservice.repository.NotificationRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.LocalDateTime

@Service
class NotificationDeliveryService(
    private val notificationRepository: NotificationRepository,
    private val notificationProcessor: NotificationProcessor,
    private val clock: Clock) {

    companion object {
        private val logger = LoggerFactory.getLogger(NotificationDeliveryService::class.java)
    }
    fun processPendingNotifications(){
        val notificationIdsToProcess = notificationRepository.findAllRequestIdByStatusForPendingNotification(
            NotificationStatus.PENDING,
            LocalDateTime.now(clock))
        for(notificationId in notificationIdsToProcess){
            try{
                notificationProcessor.processNotification(notificationId)
            } catch (e : Exception){
                logger.error("Error while processing notification: {}",notificationId, e)
            }
        }
    }
}


package com.przebarcore.notificationservice.repository

import com.przebarcore.notificationservice.entity.Notification
import com.przebarcore.notificationservice.global.enums.NotificationStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime
import java.util.UUID

interface NotificationRepository : JpaRepository<Notification, UUID> {
    @Modifying
    @Query(
        value = """
        INSERT INTO notification (
            request_id,
            reference_type,
            reference_id,
            notification_type,
            status,
            created_at,
            processed_at,
            attempt_count,
            next_attempt_at ,
            last_error 
        )
        VALUES (
            :requestId,
            :referenceType,
            :referenceId,
            :notificationType,
            'PENDING',
            :createdAt,
            NULL, 
            0, 
            NULL,
            NULL
        )
        ON CONFLICT (request_id) DO NOTHING
    """,
        nativeQuery = true
    )
    fun tryCreateNotification(
        @Param("requestId") requestId: UUID,
        @Param("referenceType") referenceType: String,
        @Param("referenceId") referenceId: Long,
        @Param("notificationType") notificationType: String,
        @Param("createdAt") createdAt: LocalDateTime,
    ): Int

    @Query("""
        select n.requestId from Notification n where n.status = :status and (n.nextAttemptAt is null or n.nextAttemptAt <= :dateTime)
    """)
    fun findAllRequestIdByStatusForPendingNotification(@Param("status") status: NotificationStatus, @Param("dateTime") dateTime: LocalDateTime): List<UUID>
}
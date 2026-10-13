package com.przebarcore.notificationservice.repository

import com.przebarcore.notificationservice.entity.Notification
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
            processed_at 
        )
        VALUES (
            :requestId,
            :referenceType,
            :referenceId,
            :notificationType,
            'PENDING',
            :createdAt,
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
}
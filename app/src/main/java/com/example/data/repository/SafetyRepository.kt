package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

data class ReportUiItem(
    val report: ReportEntity,
    val reporterName: String,
    val reportedUserName: String
)

class SafetyRepository(private val db: AppDatabase) {
    private val reportDao = db.reportDao()
    private val blockDao = db.blockDao()
    private val userDao = db.userDao()
    private val auditLogDao = db.auditLogDao()

    suspend fun reportUser(
        reporterId: String,
        reportedUserId: String,
        conversationId: String?,
        reason: String,
        description: String
    ) {
        val report = ReportEntity(
            id = "rep_" + UUID.randomUUID().toString().take(8),
            reporterId = reporterId,
            reportedUserId = reportedUserId,
            conversationId = conversationId,
            reason = reason,
            description = description
        )
        reportDao.insertReport(report)
    }

    suspend fun blockUser(blockerId: String, blockedId: String) {
        val block = BlockEntity(
            id = "blk_" + UUID.randomUUID().toString().take(8),
            blockerId = blockerId,
            blockedId = blockedId
        )
        blockDao.insertBlock(block)
    }

    suspend fun unblockUser(blockerId: String, blockedId: String) {
        blockDao.removeBlock(blockerId, blockedId)
    }

    // Admin Panel Methods
    fun getAllReportsForAdmin(): Flow<List<ReportUiItem>> {
        return reportDao.getAllReportsForAdmin().map { reports ->
            reports.map { rep ->
                val reporter = userDao.getUserById(rep.reporterId)
                val reported = userDao.getUserById(rep.reportedUserId)
                ReportUiItem(
                    report = rep,
                    reporterName = reporter?.displayName ?: "Unknown Reporter",
                    reportedUserName = reported?.displayName ?: "Unknown User"
                )
            }
        }
    }

    fun getAllUsersForAdmin(): Flow<List<UserEntity>> = userDao.getAllUsersForAdmin()

    fun getAllAuditLogs(): Flow<List<AuditLogEntity>> = auditLogDao.getAllAuditLogs()

    suspend fun updateUserStatusByAdmin(
        adminUserId: String,
        targetUserId: String,
        newStatus: AccountStatus,
        reason: String,
        reportIdToResolve: String? = null
    ) {
        val user = userDao.getUserById(targetUserId) ?: return
        val updatedUser = user.copy(accountStatus = newStatus, updatedAt = System.currentTimeMillis())
        userDao.updateUser(updatedUser)

        // Add audit log
        auditLogDao.insertAuditLog(
            AuditLogEntity(
                id = "audit_" + UUID.randomUUID().toString().take(8),
                adminUserId = adminUserId,
                targetUserId = targetUserId,
                action = newStatus.name,
                reason = reason
            )
        )

        if (!reportIdToResolve.isNullOrBlank()) {
            reportDao.updateReportStatus(reportIdToResolve, "RESOLVED")
        }
    }

    suspend fun dismissReport(reportId: String, adminUserId: String) {
        reportDao.updateReportStatus(reportId, "DISMISSED")
        auditLogDao.insertAuditLog(
            AuditLogEntity(
                id = "audit_" + UUID.randomUUID().toString().take(8),
                adminUserId = adminUserId,
                targetUserId = "",
                action = "DISMISS_REPORT",
                reason = "Report marked dismissed as non-violation"
            )
        )
    }
}

package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id")
    fun observeUserById(id: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUserById(id: String)

    // Discover users with ISOLATED AGE GROUPS! (Teens see teens, adults see adults)
    @Query("""
        SELECT * FROM users 
        WHERE ageGroup = :ageGroup 
        AND id != :currentUserId 
        AND accountStatus = 'ACTIVE' 
        AND id NOT IN (SELECT blockedId FROM blocks WHERE blockerId = :currentUserId)
        AND id NOT IN (SELECT blockerId FROM blocks WHERE blockedId = :currentUserId)
        ORDER BY lastActiveAt DESC
    """)
    fun discoverUsersByAgeGroup(currentUserId: String, ageGroup: AgeGroup): Flow<List<UserEntity>>

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsersForAdmin(): Flow<List<UserEntity>>
}

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles WHERE userId = :userId")
    suspend fun getProfile(userId: String): ProfileEntity?

    @Query("SELECT * FROM profiles WHERE userId = :userId")
    fun observeProfile(userId: String): Flow<ProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: ProfileEntity)
}

@Dao
interface PreferenceDao {
    @Query("SELECT * FROM preferences WHERE userId = :userId")
    suspend fun getPreference(userId: String): PreferenceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePreference(preference: PreferenceEntity)
}

@Dao
interface LikeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLike(like: LikeEntity)

    @Query("SELECT * FROM likes WHERE senderId = :senderId AND receiverId = :receiverId LIMIT 1")
    suspend fun getLike(senderId: String, receiverId: String): LikeEntity?

    @Query("SELECT * FROM likes WHERE senderId = :userId")
    fun getSentLikes(userId: String): Flow<List<LikeEntity>>

    @Query("DELETE FROM likes WHERE senderId = :senderId AND receiverId = :receiverId")
    suspend fun deleteLike(senderId: String, receiverId: String)
}

@Dao
interface MatchDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: MatchEntity)

    @Query("""
        SELECT * FROM matches 
        WHERE (userAId = :userId OR userBId = :userId) 
        AND status = 'ACTIVE'
        ORDER BY createdAt DESC
    """)
    fun getMatchesForUser(userId: String): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE (userAId = :userA AND userBId = :userB) OR (userAId = :userB AND userBId = :userA) LIMIT 1")
    suspend fun getMatchBetween(userA: String, userB: String): MatchEntity?
}

@Dao
interface ConversationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateConversation(conversation: ConversationEntity)

    @Query("""
        SELECT * FROM conversations 
        WHERE (user1Id = :userId OR user2Id = :userId)
        ORDER BY lastMessageTimestamp DESC
    """)
    fun getConversationsForUser(userId: String): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE (user1Id = :userA AND user2Id = :userB) OR (user1Id = :userB AND user2Id = :userA) LIMIT 1")
    suspend fun getConversationBetween(userA: String, userB: String): ConversationEntity?
}

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY createdAt ASC")
    fun getMessagesForConversation(conversationId: String): Flow<List<MessageEntity>>

    @Query("UPDATE messages SET deletedAt = :deletedAt WHERE id = :messageId")
    suspend fun softDeleteMessage(messageId: String, deletedAt: Long = System.currentTimeMillis())
}

@Dao
interface BlockDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlock(block: BlockEntity)

    @Query("DELETE FROM blocks WHERE blockerId = :blockerId AND blockedId = :blockedId")
    suspend fun removeBlock(blockerId: String, blockedId: String)

    @Query("SELECT * FROM blocks WHERE blockerId = :userId")
    fun getBlockedUsers(userId: String): Flow<List<BlockEntity>>
}

@Dao
interface ReportDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportEntity)

    @Query("SELECT * FROM reports ORDER BY createdAt DESC")
    fun getAllReportsForAdmin(): Flow<List<ReportEntity>>

    @Query("UPDATE reports SET status = :status, resolvedAt = :resolvedAt WHERE id = :reportId")
    suspend fun updateReportStatus(reportId: String, status: String, resolvedAt: Long = System.currentTimeMillis())
}

@Dao
interface SessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity)

    @Query("SELECT * FROM sessions ORDER BY createdAt DESC LIMIT 1")
    suspend fun getCurrentSession(): SessionEntity?

    @Query("SELECT * FROM sessions ORDER BY createdAt DESC LIMIT 1")
    fun observeCurrentSession(): Flow<SessionEntity?>

    @Query("DELETE FROM sessions")
    suspend fun clearSessions()
}

@Dao
interface AuditLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity)

    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogs(): Flow<List<AuditLogEntity>>
}

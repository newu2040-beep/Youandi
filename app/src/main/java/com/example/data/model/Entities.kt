package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AgeGroup {
    TEEN, // 13-17 years old
    ADULT // 18+ years old
}

enum class VerificationStatus {
    UNVERIFIED,
    EMAIL_VERIFIED,
    PROFILE_VERIFIED
}

enum class AccountStatus {
    ACTIVE,
    WARNED,
    SUSPENDED,
    BANNED
}

enum class UserRole {
    USER,
    ADMIN
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val email: String,
    val authProvider: String, // GOOGLE, EMAIL, GUEST
    val passwordHash: String? = null,
    val displayName: String,
    val dateOfBirth: String, // YYYY-MM-DD
    val age: Int,
    val ageGroup: AgeGroup,
    val gender: String = "MALE", // MALE, FEMALE, OTHER
    val targetGender: String = "FEMALE", // FEMALE, MALE, ALL
    val heightCm: Int = 175,
    val datingGoal: String = "Long-term relationship",
    val avatarUrl: String = "",
    val photosJson: String = "[]", // JSON array of photo URLs
    val bio: String = "",
    val locationArea: String = "Kathmandu area",
    val locationCity: String = "Kathmandu",
    val latitude: Double? = 27.7172,
    val longitude: Double? = 85.3240,
    val isOnboarded: Boolean = true,
    val verificationStatus: VerificationStatus = VerificationStatus.EMAIL_VERIFIED,
    val accountStatus: AccountStatus = AccountStatus.ACTIVE,
    val role: UserRole = UserRole.USER,
    val isGuest: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastActiveAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey val userId: String,
    val bio: String = "",
    val interests: String = "Travel, Music, Fitness", // comma-separated
    val hobbies: String = "Photography, Reading, Hiking",
    val personality: String = "Thoughtful & Adventurous",
    val relationshipIntention: String = "Long-term relationship",
    val heightCm: Int = 175,
    val gender: String = "MALE",
    val targetGender: String = "FEMALE",
    val photosJson: String = "[]",
    val visibility: String = "PUBLIC",
    val profileCompletion: Int = 85,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "preferences")
data class PreferenceEntity(
    @PrimaryKey val userId: String,
    val minAge: Int = 18,
    val maxAge: Int = 35,
    val distanceKm: Int = 50,
    val preferredGender: String = "FEMALE", // FEMALE, MALE, ALL
    val preferredInterests: String = "Music, Art, Travel",
    val visibilitySettings: String = "PUBLIC"
)

@Entity(tableName = "likes")
data class LikeEntity(
    @PrimaryKey val id: String,
    val senderId: String,
    val receiverId: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey val id: String,
    val userAId: String,
    val userBId: String,
    val status: String = "ACTIVE",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val user1Id: String,
    val user2Id: String,
    val lastMessage: String = "",
    val lastMessageTimestamp: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val senderId: String,
    val message: String,
    val messageType: String = "TEXT",
    val createdAt: Long = System.currentTimeMillis(),
    val readAt: Long? = null,
    val deletedAt: Long? = null
)

@Entity(tableName = "blocks")
data class BlockEntity(
    @PrimaryKey val id: String,
    val blockerId: String,
    val blockedId: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey val id: String,
    val reporterId: String,
    val reportedUserId: String,
    val conversationId: String? = null,
    val reason: String,
    val description: String,
    val status: String = "PENDING", // PENDING, RESOLVED, DISMISSED
    val createdAt: Long = System.currentTimeMillis(),
    val resolvedAt: Long? = null
)

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val isGuest: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000)
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey val id: String,
    val adminUserId: String,
    val targetUserId: String,
    val action: String, // WARN, SUSPEND, BAN, UNBAN, RESOLVE_REPORT
    val reason: String,
    val timestamp: Long = System.currentTimeMillis()
)

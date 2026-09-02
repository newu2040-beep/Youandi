package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.ConversationEntity
import com.example.data.model.LikeEntity
import com.example.data.model.MatchEntity
import com.example.data.model.MessageEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class SocialRepository(private val db: AppDatabase) {
    private val likeDao = db.likeDao()
    private val matchDao = db.matchDao()
    private val conversationDao = db.conversationDao()
    private val messageDao = db.messageDao()
    private val userDao = db.userDao()

    /**
     * Express interest (Like).
     * If the receiver already liked the sender, trigger a MUTUAL MATCH!
     * Returns true if mutual match occurred, false otherwise.
     */
    suspend fun likeUser(senderId: String, receiverId: String): Boolean {
        // Enforce age boundary check: ensure sender and receiver belong to same age group
        val sender = userDao.getUserById(senderId) ?: return false
        val receiver = userDao.getUserById(receiverId) ?: return false
        if (sender.ageGroup != receiver.ageGroup) {
            // Strictly prohibit cross-age liking
            return false
        }

        val like = LikeEntity(
            id = UUID.randomUUID().toString(),
            senderId = senderId,
            receiverId = receiverId
        )
        likeDao.insertLike(like)

        // Check if receiver previously liked sender
        val reciprocalLike = likeDao.getLike(senderId = receiverId, receiverId = senderId)
        if (reciprocalLike != null) {
            // It's a match!
            val matchId = "match_" + UUID.randomUUID().toString().take(8)
            matchDao.insertMatch(
                MatchEntity(
                    id = matchId,
                    userAId = senderId,
                    userBId = receiverId
                )
            )

            // Automatically open a conversation
            val conversationId = "conv_" + UUID.randomUUID().toString().take(8)
            val initialMsg = "It's a match! Say hello to each other."
            conversationDao.insertOrUpdateConversation(
                ConversationEntity(
                    id = conversationId,
                    user1Id = senderId,
                    user2Id = receiverId,
                    lastMessage = initialMsg,
                    lastMessageTimestamp = System.currentTimeMillis()
                )
            )

            messageDao.insertMessage(
                MessageEntity(
                    id = UUID.randomUUID().toString(),
                    conversationId = conversationId,
                    senderId = "system",
                    message = initialMsg,
                    messageType = "SYSTEM"
                )
            )
            return true
        }
        return false
    }

    fun getMatches(userId: String): Flow<List<UserEntity>> {
        return matchDao.getMatchesForUser(userId).map { matches ->
            matches.mapNotNull { match ->
                val otherId = if (match.userAId == userId) match.userBId else match.userAId
                userDao.getUserById(otherId)
            }
        }
    }

    suspend fun checkMutualMatch(userA: String, userB: String): Boolean {
        return matchDao.getMatchBetween(userA, userB) != null
    }
}

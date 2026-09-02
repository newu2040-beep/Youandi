package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.ConversationEntity
import com.example.data.model.MessageEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

data class ConversationUiItem(
    val conversation: ConversationEntity,
    val otherUser: UserEntity
)

class ChatRepository(private val db: AppDatabase) {
    private val conversationDao = db.conversationDao()
    private val messageDao = db.messageDao()
    private val userDao = db.userDao()

    fun getConversationsForUser(userId: String): Flow<List<ConversationUiItem>> {
        return conversationDao.getConversationsForUser(userId).map { convs ->
            convs.mapNotNull { conv ->
                val otherId = if (conv.user1Id == userId) conv.user2Id else conv.user1Id
                val otherUser = userDao.getUserById(otherId)
                if (otherUser != null) {
                    ConversationUiItem(conversation = conv, otherUser = otherUser)
                } else null
            }
        }
    }

    fun getMessages(conversationId: String): Flow<List<MessageEntity>> {
        return messageDao.getMessagesForConversation(conversationId)
    }

    suspend fun sendMessage(conversationId: String, senderId: String, text: String) {
        val message = MessageEntity(
            id = "msg_" + UUID.randomUUID().toString().take(8),
            conversationId = conversationId,
            senderId = senderId,
            message = text,
            messageType = "TEXT"
        )
        messageDao.insertMessage(message)

        // Update conversation summary
        val convs = conversationDao.getConversationsForUser(senderId)
        // Find existing or update
        val updatedConv = ConversationEntity(
            id = conversationId,
            user1Id = senderId,
            user2Id = "", // preserve
            lastMessage = text,
            lastMessageTimestamp = System.currentTimeMillis()
        )
    }

    suspend fun sendMessageToUser(senderId: String, receiverId: String, text: String): String {
        // Enforce cross-age check
        val sender = userDao.getUserById(senderId) ?: throw Exception("Invalid sender")
        val receiver = userDao.getUserById(receiverId) ?: throw Exception("Invalid receiver")
        if (sender.ageGroup != receiver.ageGroup) {
            throw Exception("Direct messaging across teen/adult boundary is strictly prohibited for community safety.")
        }

        var conv = conversationDao.getConversationBetween(senderId, receiverId)
        if (conv == null) {
            val newConvId = "conv_" + UUID.randomUUID().toString().take(8)
            conv = ConversationEntity(
                id = newConvId,
                user1Id = senderId,
                user2Id = receiverId,
                lastMessage = text,
                lastMessageTimestamp = System.currentTimeMillis()
            )
            conversationDao.insertOrUpdateConversation(conv)
        } else {
            conv = conv.copy(
                lastMessage = text,
                lastMessageTimestamp = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            conversationDao.insertOrUpdateConversation(conv)
        }

        val message = MessageEntity(
            id = "msg_" + UUID.randomUUID().toString().take(8),
            conversationId = conv.id,
            senderId = senderId,
            message = text
        )
        messageDao.insertMessage(message)
        return conv.id
    }

    suspend fun softDeleteMessage(messageId: String) {
        messageDao.softDeleteMessage(messageId)
    }
}

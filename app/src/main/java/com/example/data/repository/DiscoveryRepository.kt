package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.AgeGroup
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow

class DiscoveryRepository(private val db: AppDatabase) {
    private val userDao = db.userDao()

    /**
     * Strict Database-level Age Gating:
     * - Adults (18+) ONLY see Adult accounts.
     * - Teens (13-17) ONLY see Teen accounts.
     * Adults and Teens NEVER appear in each other's discovery query!
     */
    fun getDiscoverableUsers(currentUserId: String, currentAgeGroup: AgeGroup): Flow<List<UserEntity>> {
        return userDao.discoverUsersByAgeGroup(currentUserId, currentAgeGroup)
    }

    suspend fun getUserById(userId: String): UserEntity? {
        return userDao.getUserById(userId)
    }
}

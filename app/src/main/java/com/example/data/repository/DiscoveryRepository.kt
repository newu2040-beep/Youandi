package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.AgeGroup
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.math.*

class DiscoveryRepository(private val db: AppDatabase) {
    private val userDao = db.userDao()

    /**
     * Strict Database-level Age Gating:
     * - Adults (18+) ONLY see Adult accounts.
     * - Teens (13-17) ONLY see Teen accounts.
     * Adults and Teens NEVER appear in each other's discovery query!
     */
    fun getDiscoverableUsers(
        currentUserId: String,
        currentAgeGroup: AgeGroup,
        currentUser: UserEntity? = null,
        maxDistanceKm: Int = 100,
        preferredGender: String = "ALL"
    ): Flow<List<UserEntity>> {
        return userDao.discoverUsersByAgeGroup(currentUserId, currentAgeGroup).map { users ->
            users.filter { candidate ->
                // Filter by target gender if specified
                val matchesGender = when (preferredGender) {
                    "MALE" -> candidate.gender.equals("MALE", ignoreCase = true)
                    "FEMALE" -> candidate.gender.equals("FEMALE", ignoreCase = true)
                    else -> true
                }

                // Filter by distance if user location is available
                val distance = calculateDistanceKm(
                    currentUser?.latitude, currentUser?.longitude,
                    candidate.latitude, candidate.longitude
                )
                val matchesDistance = distance <= maxDistanceKm

                matchesGender && matchesDistance
            }
        }
    }

    suspend fun getUserById(userId: String): UserEntity? {
        return userDao.getUserById(userId)
    }

    companion object {
        fun calculateDistanceKm(lat1: Double?, lon1: Double?, lat2: Double?, lon2: Double?): Int {
            if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) return 8
            val r = 6371.0 // Earth radius in km
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val a = sin(dLat / 2) * sin(dLat / 2) +
                    cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                    sin(dLon / 2) * sin(dLon / 2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            return max(1, (r * c).toInt())
        }
    }
}

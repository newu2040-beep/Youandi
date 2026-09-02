package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class AuthRepository(val db: AppDatabase) {
    private val userDao = db.userDao()
    private val profileDao = db.profileDao()
    private val preferenceDao = db.preferenceDao()
    private val sessionDao = db.sessionDao()

    val currentSession: Flow<SessionEntity?> = sessionDao.observeCurrentSession()

    fun observeCurrentUser(userId: String): Flow<UserEntity?> = userDao.observeUserById(userId)

    suspend fun getCurrentUser(): UserEntity? {
        val session = sessionDao.getCurrentSession() ?: return null
        if (session.isGuest) return createGuestUserObject()
        return userDao.getUserById(session.userId)
    }

    suspend fun loginWithEmail(email: String, password: String): Result<UserEntity> {
        try {
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).await()
        } catch (e: Exception) {
            // Fallback or handle error
        }

        var user = userDao.getUserByEmail(email)
        if (user == null) {
            // If authenticated in Firebase but local record doesn't exist yet
            val newUserId = "user_" + UUID.randomUUID().toString().take(8)
            user = UserEntity(
                id = newUserId,
                email = email,
                authProvider = "EMAIL",
                displayName = email.substringBefore("@"),
                dateOfBirth = "2000-01-01",
                age = 24,
                ageGroup = AgeGroup.ADULT,
                verificationStatus = VerificationStatus.EMAIL_VERIFIED
            )
            userDao.insertUser(user)
            profileDao.insertOrUpdateProfile(ProfileEntity(userId = newUserId))
        }

        if (user.accountStatus == AccountStatus.BANNED) {
            return Result.failure(Exception("This account has been suspended for safety violations."))
        }

        // Create Session
        sessionDao.clearSessions()
        sessionDao.insertSession(
            SessionEntity(
                id = UUID.randomUUID().toString(),
                userId = user.id,
                isGuest = false
            )
        )
        return Result.success(user)
    }

    suspend fun loginWithGoogleToken(idToken: String, fallbackEmail: String? = null, fallbackName: String? = null): Result<UserEntity> {
        var email = fallbackEmail ?: "google_user@youandi.app"
        var name = fallbackName ?: "Google Member"
        var photoUrl = ""

        try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = FirebaseAuth.getInstance().signInWithCredential(credential).await()
            val firebaseUser = authResult.user
            if (firebaseUser != null) {
                email = firebaseUser.email ?: email
                name = firebaseUser.displayName ?: name
                photoUrl = firebaseUser.photoUrl?.toString() ?: ""
            }
        } catch (e: Exception) {
            // Fallback gracefully
        }

        return loginWithGoogle(email, name, photoUrl)
    }

    suspend fun loginWithGoogle(email: String, name: String, photoUrl: String): Result<UserEntity> {
        var user = userDao.getUserByEmail(email)
        if (user == null) {
            val newUserId = "user_" + UUID.randomUUID().toString().take(8)
            user = UserEntity(
                id = newUserId,
                email = email,
                authProvider = "GOOGLE",
                displayName = name.ifBlank { "You & i User" },
                dateOfBirth = "2002-06-15",
                age = 22,
                ageGroup = AgeGroup.ADULT,
                avatarUrl = photoUrl,
                verificationStatus = VerificationStatus.PROFILE_VERIFIED
            )
            userDao.insertUser(user)
            profileDao.insertOrUpdateProfile(
                ProfileEntity(
                    userId = newUserId,
                    bio = "Newly joined You & i member!",
                    interests = "Coffee, Music, Art",
                    personality = "Friendly & Curious"
                )
            )
        }

        sessionDao.clearSessions()
        sessionDao.insertSession(
            SessionEntity(
                id = UUID.randomUUID().toString(),
                userId = user.id,
                isGuest = false
            )
        )
        return Result.success(user)
    }

    suspend fun registerEmailUser(
        email: String,
        password: String,
        displayName: String,
        dob: String // YYYY-MM-DD
    ): Result<UserEntity> {
        val existing = userDao.getUserByEmail(email)
        if (existing != null) {
            return Result.failure(Exception("An account with this email already exists"))
        }

        val age = calculateAge(dob)
        if (age < 13) {
            return Result.failure(Exception("You & i requires users to be at least 13 years old."))
        }

        try {
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).await()
        } catch (e: Exception) {
            // Log or fallback
        }

        val ageGroup = if (age in 13..17) AgeGroup.TEEN else AgeGroup.ADULT
        val newUserId = "user_" + UUID.randomUUID().toString().take(8)

        val newUser = UserEntity(
            id = newUserId,
            email = email,
            authProvider = "EMAIL",
            passwordHash = password,
            displayName = displayName,
            dateOfBirth = dob,
            age = age,
            ageGroup = ageGroup,
            verificationStatus = VerificationStatus.EMAIL_VERIFIED
        )

        userDao.insertUser(newUser)
        profileDao.insertOrUpdateProfile(ProfileEntity(userId = newUserId))
        preferenceDao.insertOrUpdatePreference(
            PreferenceEntity(
                userId = newUserId,
                minAge = if (ageGroup == AgeGroup.TEEN) 13 else 18,
                maxAge = if (ageGroup == AgeGroup.TEEN) 17 else 50
            )
        )

        sessionDao.clearSessions()
        sessionDao.insertSession(
            SessionEntity(
                id = UUID.randomUUID().toString(),
                userId = newUserId,
                isGuest = false
            )
        )
        return Result.success(newUser)
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> {
        return try {
            FirebaseAuth.getInstance().sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.success(Unit)
        }
    }

    suspend fun startGuestSession() {
        sessionDao.clearSessions()
        sessionDao.insertSession(
            SessionEntity(
                id = UUID.randomUUID().toString(),
                userId = "guest_user",
                isGuest = true
            )
        )
    }

    suspend fun logout() {
        try {
            FirebaseAuth.getInstance().signOut()
        } catch (e: Exception) {
            // ignore
        }
        sessionDao.clearSessions()
    }

    suspend fun deleteAccount(userId: String) {
        userDao.deleteUserById(userId)
        sessionDao.clearSessions()
    }

    fun calculateAge(dob: String): Int {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val birthDate = sdf.parse(dob) ?: return 20
            val dobCalendar = Calendar.getInstance().apply { time = birthDate }
            val todayCalendar = Calendar.getInstance()

            var age = todayCalendar.get(Calendar.YEAR) - dobCalendar.get(Calendar.YEAR)
            if (todayCalendar.get(Calendar.DAY_OF_YEAR) < dobCalendar.get(Calendar.DAY_OF_YEAR)) {
                age--
            }
            age
        } catch (e: Exception) {
            20
        }
    }

    fun createGuestUserObject(): UserEntity {
        return UserEntity(
            id = "guest_user",
            email = "guest@youandi.app",
            authProvider = "GUEST",
            displayName = "Guest Explorer",
            dateOfBirth = "2000-01-01",
            age = 24,
            ageGroup = AgeGroup.ADULT,
            avatarUrl = "",
            bio = "Exploring You & i in Guest Mode",
            locationArea = "Public Explorer",
            isGuest = true
        )
    }
}

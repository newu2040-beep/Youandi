package com.example.data.local

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        ProfileEntity::class,
        PreferenceEntity::class,
        LikeEntity::class,
        MatchEntity::class,
        ConversationEntity::class,
        MessageEntity::class,
        BlockEntity::class,
        ReportEntity::class,
        SessionEntity::class,
        AuditLogEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun profileDao(): ProfileDao
    abstract fun preferenceDao(): PreferenceDao
    abstract fun likeDao(): LikeDao
    abstract fun matchDao(): MatchDao
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun blockDao(): BlockDao
    abstract fun reportDao(): ReportDao
    abstract fun sessionDao(): SessionDao
    abstract fun auditLogDao(): AuditLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "you_and_i_db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback(context))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        seedDefaultData(database)
                    }
                }
            }
        }

        private suspend fun seedDefaultData(db: AppDatabase) {
            val userDao = db.userDao()
            val profileDao = db.profileDao()

            // 1. Current Demo User (Rahul - Adult, 24)
            val rahulUser = UserEntity(
                id = "user_rahul",
                email = "rahul@youandi.app",
                authProvider = "EMAIL",
                displayName = "Rahul Sharma",
                dateOfBirth = "2000-05-15",
                age = 24,
                ageGroup = AgeGroup.ADULT,
                avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d",
                bio = "Architect & coffee lover based in Kathmandu. Always down for deep chats and hiking.",
                locationArea = "Kathmandu area",
                verificationStatus = VerificationStatus.PROFILE_VERIFIED,
                accountStatus = AccountStatus.ACTIVE,
                role = UserRole.USER
            )
            userDao.insertUser(rahulUser)
            profileDao.insertOrUpdateProfile(
                ProfileEntity(
                    userId = "user_rahul",
                    bio = rahulUser.bio,
                    interests = "Architecture, Coffee, Hiking, Music, Photography",
                    hobbies = "Designing, Latte art, Camping",
                    personality = "Thoughtful & Creative",
                    relationshipIntention = "Meaningful Connections"
                )
            )

            // 2. Demo Admin User
            val adminUser = UserEntity(
                id = "user_admin",
                email = "admin@youandi.app",
                authProvider = "EMAIL",
                displayName = "You & i Safety Admin",
                dateOfBirth = "1995-01-01",
                age = 29,
                ageGroup = AgeGroup.ADULT,
                avatarUrl = "",
                bio = "Community Safety & Trust Officer",
                locationArea = "Official Admin",
                verificationStatus = VerificationStatus.PROFILE_VERIFIED,
                accountStatus = AccountStatus.ACTIVE,
                role = UserRole.ADMIN
            )
            userDao.insertUser(adminUser)

            // 3. ADULT Discovery Profiles
            val adultProfiles = listOf(
                UserEntity(
                    id = "user_sophie",
                    email = "sophie@example.com",
                    authProvider = "EMAIL",
                    displayName = "Sophie Varma",
                    dateOfBirth = "2002-08-20",
                    age = 22,
                    ageGroup = AgeGroup.ADULT,
                    avatarUrl = "img_profile_sophie",
                    bio = "UX Designer in Kathmandu. Loves indie acoustic music, ceramic painting, and rooftop matcha dates.",
                    locationArea = "Kathmandu area",
                    verificationStatus = VerificationStatus.PROFILE_VERIFIED
                ),
                UserEntity(
                    id = "user_aarav",
                    email = "aarav@example.com",
                    authProvider = "EMAIL",
                    displayName = "Aarav Shrestha",
                    dateOfBirth = "2001-03-12",
                    age = 23,
                    ageGroup = AgeGroup.ADULT,
                    avatarUrl = "img_profile_aarav",
                    bio = "Dreamer & Fitness Enthusiast in Pokhara. Always up for new mountain adventures and genuine conversations.",
                    locationArea = "Pokhara area",
                    verificationStatus = VerificationStatus.PROFILE_VERIFIED
                ),
                UserEntity(
                    id = "user_maya",
                    email = "maya@example.com",
                    authProvider = "EMAIL",
                    displayName = "Maya Gurung",
                    dateOfBirth = "2002-11-05",
                    age = 22,
                    ageGroup = AgeGroup.ADULT,
                    avatarUrl = "",
                    bio = "Botany student & tea collector. Looking for sincere friendships and calm evening walks.",
                    locationArea = "Lalitpur area",
                    verificationStatus = VerificationStatus.EMAIL_VERIFIED
                ),
                UserEntity(
                    id = "user_rohan",
                    email = "rohan@example.com",
                    authProvider = "EMAIL",
                    displayName = "Rohan Karki",
                    dateOfBirth = "2000-09-18",
                    age = 24,
                    ageGroup = AgeGroup.ADULT,
                    avatarUrl = "",
                    bio = "Software developer by day, acoustic guitarist by night. Passioned about open source and vinyl records.",
                    locationArea = "Pokhara area",
                    verificationStatus = VerificationStatus.PROFILE_VERIFIED
                )
            )

            for (user in adultProfiles) {
                userDao.insertUser(user)
                profileDao.insertOrUpdateProfile(
                    ProfileEntity(
                        userId = user.id,
                        bio = user.bio,
                        interests = "Travel, Music, Fitness, Photography, Books",
                        hobbies = "Acoustic music, Trekking, Espresso",
                        personality = "Warm & Authentic",
                        relationshipIntention = "Friendship & Dating"
                    )
                )
            }

            // 4. TEEN Discovery Profiles (Strictly isolated from Adults)
            val teenProfiles = listOf(
                UserEntity(
                    id = "user_teen_anita",
                    email = "anita_teen@example.com",
                    authProvider = "EMAIL",
                    displayName = "Anita P.",
                    dateOfBirth = "2008-04-10",
                    age = 16,
                    ageGroup = AgeGroup.TEEN,
                    avatarUrl = "",
                    bio = "High school senior. Passionate about debate, sci-fi novels, and coding Python projects.",
                    locationArea = "Kathmandu area",
                    verificationStatus = VerificationStatus.EMAIL_VERIFIED
                ),
                UserEntity(
                    id = "user_teen_sam",
                    email = "sam_teen@example.com",
                    authProvider = "EMAIL",
                    displayName = "Sam K.",
                    dateOfBirth = "2009-02-14",
                    age = 15,
                    ageGroup = AgeGroup.TEEN,
                    avatarUrl = "",
                    bio = "Skateboarding, digital art, and lo-fi beats. Looking for study buddies and chill gaming friends.",
                    locationArea = "Lalitpur area",
                    verificationStatus = VerificationStatus.EMAIL_VERIFIED
                )
            )

            for (teen in teenProfiles) {
                userDao.insertUser(teen)
                profileDao.insertOrUpdateProfile(
                    ProfileEntity(
                        userId = teen.id,
                        bio = teen.bio,
                        interests = "Coding, Books, Art, Gaming, Skateboarding",
                        hobbies = "Study sessions, Lo-fi music",
                        personality = "Creative & Curious",
                        relationshipIntention = "Teen Friendship & Study Buddies"
                    )
                )
            }

            // 5. Create default Session for immediate seamless preview
            db.sessionDao().insertSession(
                SessionEntity(
                    id = "session_default",
                    userId = "user_rahul",
                    isGuest = false
                )
            )
        }
    }
}

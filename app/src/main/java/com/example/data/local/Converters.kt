package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.AccountStatus
import com.example.data.model.AgeGroup
import com.example.data.model.UserRole
import com.example.data.model.VerificationStatus

class Converters {
    @TypeConverter
    fun fromAgeGroup(value: AgeGroup): String = value.name

    @TypeConverter
    fun toAgeGroup(value: String): AgeGroup = runCatching { AgeGroup.valueOf(value) }.getOrDefault(AgeGroup.ADULT)

    @TypeConverter
    fun fromVerificationStatus(value: VerificationStatus): String = value.name

    @TypeConverter
    fun toVerificationStatus(value: String): VerificationStatus = runCatching { VerificationStatus.valueOf(value) }.getOrDefault(VerificationStatus.UNVERIFIED)

    @TypeConverter
    fun fromAccountStatus(value: AccountStatus): String = value.name

    @TypeConverter
    fun toAccountStatus(value: String): AccountStatus = runCatching { AccountStatus.valueOf(value) }.getOrDefault(AccountStatus.ACTIVE)

    @TypeConverter
    fun fromUserRole(value: UserRole): String = value.name

    @TypeConverter
    fun toUserRole(value: String): UserRole = runCatching { UserRole.valueOf(value) }.getOrDefault(UserRole.USER)
}

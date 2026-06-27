package com.neofit.data.repository

import com.neofit.data.local.dao.UserDao
import com.neofit.data.local.toDomain
import com.neofit.data.local.toEntity
import com.neofit.domain.model.UserProfile
import com.neofit.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
) : UserRepository {

    override fun observeProfile(): Flow<UserProfile?> =
        userDao.observe().map { it?.toDomain() }

    override suspend fun getProfile(): UserProfile? = userDao.get()?.toDomain()

    override suspend fun upsertProfile(profile: UserProfile) =
        userDao.upsert(profile.toEntity())
}

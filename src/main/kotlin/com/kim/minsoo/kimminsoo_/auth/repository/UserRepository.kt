package com.kim.minsoo.kimminsoo_.auth.repository

import com.kim.minsoo.kimminsoo_.auth.domain.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserRepository: JpaRepository<UserEntity, UUID> {
    fun findByUsername(username: String): UserEntity?
}
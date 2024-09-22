package com.kim.minsoo.kimminsoo_.auth.domain.entity

import jakarta.persistence.*
import java.util.*


@Entity
@Table(name = "users")
data class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false, unique = true, length = 50)
    val username: String,

    @Column(nullable = false)
    var password: String,

    @Column(nullable = false)
    var enabled: Boolean = true,

    @Column(name = "account_non_locked", nullable = false)
    var accountNonLocked: Boolean = true,

    @Column(name = "failed_attempt", nullable = false)
    var failedAttempt: Int = 0,

    @Column(name = "lock_time")
    var lockTime: Date? = null
)

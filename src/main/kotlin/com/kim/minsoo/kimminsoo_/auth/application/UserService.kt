package com.kim.minsoo.kimminsoo_.auth.application

import com.kim.minsoo.kimminsoo_.auth.domain.entity.UserEntity
import com.kim.minsoo.kimminsoo_.auth.repository.UserRepository
import org.springframework.security.core.userdetails.User
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    private val MAX_FAILED_ATTEMPTS = 5
    private val LOCK_TIME_DURATION = 15 * 60 * 1000L // 15분

    fun registerUser(username: String, rawPassword: String) {
        if (!isValidPassword(rawPassword)) {
            throw IllegalArgumentException("비밀번호 정책에 맞지 않습니다.")
        }
        val encodedPassword = passwordEncoder.encode(rawPassword)
        val user = UserEntity(username = username, password = encodedPassword)
        userRepository.save(user)
    }

    fun isValidPassword(password: String): Boolean {
        val passwordPattern = Regex("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*]).{8,}$")
        return passwordPattern.matches(password)
    }

    fun increaseFailedAttempts(user: UserEntity) {
        val newFailAttempts = user.failedAttempt + 1
        user.failedAttempt = newFailAttempts
        if (newFailAttempts >= MAX_FAILED_ATTEMPTS) {
            lock(user)
        }
        userRepository.save(user)
    }

    fun resetFailedAttempts(user: UserEntity) {
        user.failedAttempt = 0
        userRepository.save(user)
    }

    fun lock(user: UserEntity) {
        user.accountNonLocked = false
        user.lockTime = Date()
        userRepository.save(user)
    }

    fun unlockWhenTimeExpired(user: UserEntity): Boolean {
        val lockTimeInMillis = user.lockTime?.time ?: return false
        val currentTimeInMillis = System.currentTimeMillis()

        if (lockTimeInMillis + LOCK_TIME_DURATION < currentTimeInMillis) {
            user.accountNonLocked = true
            user.failedAttempt = 0
            user.lockTime = null
            userRepository.save(user)
            return true
        }
        return false
    }
}

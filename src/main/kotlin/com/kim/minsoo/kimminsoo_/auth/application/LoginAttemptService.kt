package com.kim.minsoo.kimminsoo_.auth.application

import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class LoginAttemptService {

    private val MAX_ATTEMPTS = 10
    private val BLOCK_TIME = 10 * 60 * 1000L // 10분
    private val attemptsCache = ConcurrentHashMap<String, Attempt>()

    data class Attempt(var attempts: Int, var lastAttemptTime: Long)

    fun loginFailed(ip: String) {
        val attempt = attemptsCache.getOrDefault(ip, Attempt(0, System.currentTimeMillis()))
        attempt.attempts++
        attempt.lastAttemptTime = System.currentTimeMillis()
        attemptsCache[ip] = attempt
    }

    fun isBlocked(ip: String): Boolean {
        val attempt = attemptsCache[ip] ?: return false
        if (attempt.attempts >= MAX_ATTEMPTS) {
            val timePassed = System.currentTimeMillis() - attempt.lastAttemptTime
            if (timePassed < BLOCK_TIME) {
                return true
            } else {
                attemptsCache.remove(ip)
                return false
            }
        }
        return false
    }

    fun loginSucceeded(ip: String) {
        attemptsCache.remove(ip)
    }
}

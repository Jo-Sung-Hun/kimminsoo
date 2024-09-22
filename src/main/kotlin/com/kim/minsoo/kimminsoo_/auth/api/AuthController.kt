package com.kim.minsoo.kimminsoo_.auth.api

import com.kim.minsoo.kimminsoo_.auth.application.JwtTokenProvider
import com.kim.minsoo.kimminsoo_.auth.application.LoginAttemptService
import com.kim.minsoo.kimminsoo_.auth.application.UserService
import com.kim.minsoo.kimminsoo_.auth.repository.UserRepository
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.annotation.*

@RestController
class AuthController(
    private val authenticationManager: AuthenticationManager,
    private val jwtTokenProvider: JwtTokenProvider,
    private val loginAttemptService: LoginAttemptService,
    private val userService: UserService,
    private val userRepository: UserRepository
) {

    @PostMapping("/register")
    fun register(@RequestBody request: RegisterRequest): ResponseEntity<String> {
        println("register")
        userService.registerUser(request.username, request.password)
        return ResponseEntity.ok("회원가입이 완료되었습니다.")
    }

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest, requestHttp: HttpServletRequest): ResponseEntity<Any> {
        val ip = requestHttp.remoteAddr
        if (loginAttemptService.isBlocked(ip)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("해당 IP에서의 로그인 시도가 일시적으로 차단되었습니다.")
        }

        try {
            val username = request.username
            val password = request.password

            val authenticationToken = UsernamePasswordAuthenticationToken(username, password)
            val authentication = authenticationManager.authenticate(authenticationToken)

            val user = userRepository.findByUsername(username)
            if (user != null) {
                userService.resetFailedAttempts(user)
            }

            val token = jwtTokenProvider.createToken(authentication.name)
            loginAttemptService.loginSucceeded(ip)

            val response = mapOf("username" to username, "token" to token)
            return ResponseEntity.ok(response)
        } catch (ex: AuthenticationException) {
            val username = request.username
            val user = userRepository.findByUsername(username)
            if (user != null) {
                userService.increaseFailedAttempts(user)
            }
            loginAttemptService.loginFailed(ip)
            throw BadCredentialsException("인증 실패")
        }
    }
}

data class RegisterRequest(
    val username: String,
    val password: String
)

data class LoginRequest(
    val username: String,
    val password: String
)

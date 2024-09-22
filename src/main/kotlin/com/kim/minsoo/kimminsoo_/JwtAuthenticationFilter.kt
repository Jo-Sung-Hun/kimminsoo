package com.kim.minsoo.kimminsoo_

import com.kim.minsoo.kimminsoo_.auth.application.JwtTokenProvider
import com.kim.minsoo.kimminsoo_.auth.application.LoginAttemptService
import com.kim.minsoo.kimminsoo_.auth.application.UserService
import com.kim.minsoo.kimminsoo_.auth.repository.UserRepository
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

class JwtAuthenticationFilter(
    private val authenticationManager: AuthenticationManager,
    private val jwtTokenProvider: JwtTokenProvider,
    private val loginAttemptService: LoginAttemptService,
    private val userService: UserService,
    private val userRepository: UserRepository
) : UsernamePasswordAuthenticationFilter() {

    init {
        setFilterProcessesUrl("/login")
    }

    override fun attemptAuthentication(request: HttpServletRequest, response: HttpServletResponse): Authentication {
        val ip = request.remoteAddr
        if (loginAttemptService.isBlocked(ip)) {
            throw RuntimeException("해당 IP에서의 로그인 시도가 일시적으로 차단되었습니다.")
        }

        val username = request.getParameter("username")
        val password = request.getParameter("password")

        val authToken = UsernamePasswordAuthenticationToken(username, password)

        try {
            val auth = authenticationManager.authenticate(authToken)
            return auth
        } catch (ex: AuthenticationException) {
            val user = userRepository.findByUsername(username)
            if (user != null) {
                userService.increaseFailedAttempts(user)
            }
            loginAttemptService.loginFailed(ip)
            throw BadCredentialsException("인증 실패")
        }
    }
    override fun successfulAuthentication(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        chain: jakarta.servlet.FilterChain?,
        authResult: Authentication?
    ) {
        val ip = request?.remoteAddr
        if (ip != null) {
            loginAttemptService.loginSucceeded(ip)
        }

        val username = authResult?.name
        if (username != null) {
            val user = userRepository.findByUsername(username)
            if (user != null) {
                userService.resetFailedAttempts(user)
            }

            val token = jwtTokenProvider.createToken(username)

            response?.addHeader("Authorization", "Bearer $token")
        }
    }

}

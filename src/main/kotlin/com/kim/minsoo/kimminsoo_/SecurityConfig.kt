package com.kim.minsoo.kimminsoo_

import com.kim.minsoo.kimminsoo_.auth.application.*
import com.kim.minsoo.kimminsoo_.auth.repository.UserRepository
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtTokenProvider: JwtTokenProvider,
    private val loginAttemptService: LoginAttemptService,
    private val passwordEncoder: PasswordEncoder,
    private val customUserDetailsService: CustomUserDetailsService,
    private val userService: UserService,
    private val userRepository: UserRepository,
    private val authenticationConfiguration: AuthenticationConfiguration
) {

    @Bean
    fun authenticationManager(): AuthenticationManager {
        return authenticationConfiguration.authenticationManager
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { authorize ->
                authorize
                    .requestMatchers("/login", "/register").permitAll()
                    .anyRequest().permitAll()
            }
    /*        .exceptionHandling { exceptionHandling ->
                exceptionHandling.authenticationEntryPoint { _, response, authException ->
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.message)
                }
            }*/
            .addFilterBefore(JwtTokenFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter::class.java)
            /*.addFilterBefore(
                JwtAuthenticationFilter(
                    authenticationManager(),
                    jwtTokenProvider,
                    loginAttemptService,
                    userService,
                    userRepository
                ),
                JwtTokenFilter::class.java
            )*/
     /*       .headers { headers ->
                headers
                    .contentSecurityPolicy { it.policyDirectives("default-src 'self'") }
                    .httpStrictTransportSecurity { it.includeSubDomains(true).maxAgeInSeconds(31536000) }
                    .frameOptions { it.deny() }
            }*/

        return http.build()
    }
}
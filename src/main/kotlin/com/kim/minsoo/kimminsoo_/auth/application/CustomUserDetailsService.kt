package com.kim.minsoo.kimminsoo_.auth.application

import com.kim.minsoo.kimminsoo_.auth.repository.UserRepository
import org.springframework.security.authentication.LockedException
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository,
    private val userService: UserService
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByUsername(username)
            ?: throw UsernameNotFoundException("사용자를 찾을 수 없습니다.")

        if (!user.accountNonLocked) {
            val unlocked = userService.unlockWhenTimeExpired(user)
            if (!unlocked) {
                throw LockedException("계정이 잠겨 있습니다. 잠시 후 다시 시도해주세요.")
            }
        }

        return User(
            user.username,
            user.password,
            user.enabled,
            true,
            true,
            user.accountNonLocked,
            emptyList()
        )
    }
}

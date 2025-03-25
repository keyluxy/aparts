package com.example.apartapp.domain.usecases

import com.example.apartapp.domain.repository.AuthRepository
import javax.inject.Inject

class LoginByEmailUseCase @Inject constructor(private val authRepository: AuthRepository) {
   suspend operator fun invoke(email: String, password: String): Result<String> {
       return kotlin.runCatching { authRepository.login(email, password) }
   }
}
package com.example.apartapp.domain.usecases

import com.example.apartapp.domain.repository.AuthRepository
import javax.inject.Inject

class RegistrByEmailUseCase @Inject constructor(private val authRepository: AuthRepository) {
    suspend operator fun invoke(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        middleName: String?
    ): Result<Int> {
        return kotlin.runCatching {
            authRepository.register(
                email,
                password,
                firstName,
                lastName,
                middleName
            ) }
    }
}

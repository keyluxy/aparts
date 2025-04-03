package com.example.apartapp.domain.usecases

import com.example.apartapp.domain.repository.ParsingRepository
import javax.inject.Inject

class StartParsingUseCase @Inject constructor(
    private val parsingRepository: ParsingRepository
) {
    suspend operator fun invoke(): Result<String> {
        return parsingRepository.startParsing()
    }
} 
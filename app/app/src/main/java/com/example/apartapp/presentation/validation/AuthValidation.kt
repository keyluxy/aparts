package com.example.apartapp.presentation.validation

sealed class ValidationResult {
    data object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}

object AuthValidation {
    private fun validateEmail(email: String): ValidationResult {
        return when {
            email.isBlank() -> ValidationResult.Error("Email не может быть пустым")
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> 
                ValidationResult.Error("Некорректный формат email")
            else -> ValidationResult.Success
        }
    }

    private fun validatePassword(password: String): ValidationResult {
        return when {
            password.isBlank() -> ValidationResult.Error("Пароль не может быть пустым")
            password.length < 6 -> ValidationResult.Error("Пароль должен содержать минимум 6 символов")
            !password.any { it.isDigit() } -> ValidationResult.Error("Пароль должен содержать хотя бы одну цифру")
            !password.any { it.isUpperCase() } -> ValidationResult.Error("Пароль должен содержать хотя бы одну заглавную букву")
            else -> ValidationResult.Success
        }
    }

    private fun validateName(name: String, fieldName: String): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult.Error("$fieldName не может быть пустым")
            name.length < 2 -> ValidationResult.Error("$fieldName должен содержать минимум 2 символа")
            !name.all { it.isLetter() } -> ValidationResult.Error("$fieldName должен содержать только буквы")
            else -> ValidationResult.Success
        }
    }

    fun validateRegistration(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        middleName: String?
    ): ValidationResult {
        val emailValidation = validateEmail(email)
        if (emailValidation is ValidationResult.Error) return emailValidation

        val passwordValidation = validatePassword(password)
        if (passwordValidation is ValidationResult.Error) return passwordValidation

        val firstNameValidation = validateName(firstName, "Имя")
        if (firstNameValidation is ValidationResult.Error) return firstNameValidation

        val lastNameValidation = validateName(lastName, "Фамилия")
        if (lastNameValidation is ValidationResult.Error) return lastNameValidation

        middleName?.let {
            val middleNameValidation = validateName(it, "Отчество")
            if (middleNameValidation is ValidationResult.Error) return middleNameValidation
        }

        return ValidationResult.Success
    }

    fun validateLogin(email: String, password: String): ValidationResult {
        val emailValidation = validateEmail(email)
        if (emailValidation is ValidationResult.Error) return emailValidation

        val passwordValidation = validatePassword(password)
        if (passwordValidation is ValidationResult.Error) return passwordValidation

        return ValidationResult.Success
    }
} 
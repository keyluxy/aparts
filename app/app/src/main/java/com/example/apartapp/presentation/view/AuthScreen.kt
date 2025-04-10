package com.example.apartapp.presentation.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.apartapp.presentation.viewmodel.AuthViewModel
import androidx.compose.ui.Modifier
import com.example.apartapp.presentation.viewmodel.state.AuthState


@Composable
fun AuthScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    onAuthSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var middleName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoginMode by remember { mutableStateOf(true) }

    val authState = authViewModel.authState.collectAsState().value

    when (authState) {
        is AuthState.LoggedIn -> {
            onAuthSuccess()
        }

        is AuthState.Registered -> {
            onAuthSuccess()
        }

        else -> {}
    }

    RegScreen(
        email = email,
        password = password,
        firstName = firstName,
        lastName = lastName,
        middleName = middleName,
        isLoginMode = isLoginMode,
        authState = authState,
        onEmailChange = { email = it },
        onPasswordChange = { password = it },
        onFirstNameChange = { firstName = it },
        onLastNameChange = { lastName = it },
        onMiddleNameChange = { middleName = it },
        onToggleMode = { isLoginMode = !isLoginMode },
        onAuthAction = {
            if (isLoginMode) {
                authViewModel.login(email, password)
            } else {
                authViewModel.register(
                    email,
                    password,
                    firstName,
                    lastName,
                    middleName.ifBlank { null })
            }
        }
    )
}

@Composable
fun RegScreen(
    email: String,
    password: String,
    firstName: String,
    lastName: String,
    middleName: String,
    isLoginMode: Boolean,
    authState: AuthState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onMiddleNameChange: (String) -> Unit,
    onToggleMode: () -> Unit,
    onAuthAction: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = if (isLoginMode) "Login" else "Register",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text(text = "Email") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text(text = "Password") },
                modifier = Modifier.fillMaxWidth()
            )

            if (!isLoginMode) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = firstName,
                    onValueChange = onFirstNameChange,
                    label = { Text(text = "First Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = lastName,
                    onValueChange = onLastNameChange,
                    label = { Text(text = "Last Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = middleName,
                    onValueChange = onMiddleNameChange,
                    label = { Text(text = "Middle Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onAuthAction,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = if (isLoginMode) "Login" else "Register")
            }

            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onToggleMode) {
                Text(text = if (isLoginMode) "Don't have an account? Register" else "Already have an account? Login")
            }

            when (authState) {
                is AuthState.Loading -> CircularProgressIndicator(
                    modifier = Modifier
                        .padding(16.dp))
                is AuthState.Error -> Text(
                    text = "Error: ${authState.message}",
                    color = MaterialTheme.colorScheme.error
                )

                else -> {}
            }
        }
    }
}


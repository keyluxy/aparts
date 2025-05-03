package com.example.apartapp.presentation.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.apartapp.presentation.viewmodel.AuthViewModel
import com.example.apartapp.presentation.viewmodel.state.AuthState

@Composable
fun AuthScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    onAuthSuccess: (userId: Int) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var middleName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoginMode by remember { mutableStateOf(true) }

    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Registered -> onAuthSuccess((authState as AuthState.Registered).userId)
            is AuthState.LoggedIn -> {
                val userId =
                    authViewModel.getUserIdFromToken((authState as AuthState.LoggedIn).token)
                onAuthSuccess(userId)
            }

            else -> {}
        }
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
                    middleName.ifBlank { null }
                )
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
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isLoginMode) "Welcome Back" else "Create Account",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("Password") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation()
            )

            if (!isLoginMode) {
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = firstName,
                    onValueChange = onFirstNameChange,
                    label = { Text("First Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = lastName,
                    onValueChange = onLastNameChange,
                    label = { Text("Last Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = middleName,
                    onValueChange = onMiddleNameChange,
                    label = { Text("Middle Name (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onAuthAction,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = if (isLoginMode) "Sign In" else "Sign Up")
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = onToggleMode) {
                Text(
                    text = if (isLoginMode)
                        "Don't have an account? Sign Up"
                    else
                        "Already have an account? Sign In",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (authState) {
                is AuthState.Loading -> CircularProgressIndicator()
                is AuthState.Error -> Text(
                    text = authState.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )

                else -> {}
            }
        }
    }
}

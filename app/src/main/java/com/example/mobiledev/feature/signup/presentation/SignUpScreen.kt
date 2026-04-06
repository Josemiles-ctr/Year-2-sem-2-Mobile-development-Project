package com.example.mobiledev.feature.signup.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mobiledev.R
import com.example.mobiledev.ui.components.AuthInputField
import com.example.mobiledev.ui.components.AuthScreenContainer
import com.example.mobiledev.ui.theme.MobileDevTheme

@Composable
fun SignUpRoute(
    viewModel: SignUpViewModel,
    onLoginClick: () -> Unit = {},
    onAuthSuccess: () -> Unit = {}
) {
    val uiState by viewModel.signUpUiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.navigationEvents.collectLatest { event ->
            when (event) {
                SignUpViewModel.NavigationEvent.NavigateToDashboard -> {
                    onAuthSuccess()
                    viewModel.onEvent(SignUpEvent.ClearFeedback)
                }
            }
        }
    }

    SignUpScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onLoginClick = onLoginClick
    )
}

@Composable
fun SignUpScreen(
    uiState: SignUpUiState,
    onEvent: (SignUpEvent) -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AuthScreenContainer(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SignUpFormCard(uiState = uiState, onEvent = onEvent)
            LoginPromptCard(onLoginClick = onLoginClick)
        }
    }
}

@Composable
private fun SignUpFormCard(
    uiState: SignUpUiState,
    onEvent: (SignUpEvent) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.sign_up_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            AuthInputField(
                value = uiState.fullName,
                onValueChange = { onEvent(SignUpEvent.FullNameChanged(it)) },
                label = stringResource(R.string.full_name_label),
                modifier = Modifier.fillMaxWidth()
            )

            AuthInputField(
                value = uiState.phoneNumber,
                onValueChange = { onEvent(SignUpEvent.PhoneNumberChanged(it)) },
                label = stringResource(R.string.phone_number_label),
                modifier = Modifier.fillMaxWidth()
            )

            AuthInputField(
                value = uiState.email,
                onValueChange = { onEvent(SignUpEvent.EmailChanged(it)) },
                label = stringResource(R.string.email_label),
                modifier = Modifier.fillMaxWidth()
            )

            AuthInputField(
                value = uiState.password,
                onValueChange = { onEvent(SignUpEvent.PasswordChanged(it)) },
                label = stringResource(R.string.password_label),
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            AuthInputField(
                value = uiState.confirmPassword,
                onValueChange = { onEvent(SignUpEvent.ConfirmPasswordChanged(it)) },
                label = stringResource(R.string.confirm_password_label),
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            val feedback = uiState.errorMessage ?: uiState.successMessage
            if (!feedback.isNullOrBlank()) {
                Text(
                    text = feedback,
                    color = if (uiState.errorMessage != null) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Button(
                onClick = { onEvent(SignUpEvent.Submit) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(text = stringResource(R.string.create_account_label))
            }
        }
    }
}

@Composable
private fun LoginPromptCard(onLoginClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.already_have_account_label),
                style = MaterialTheme.typography.bodySmall
            )
            OutlinedButton(onClick = onLoginClick) {
                Text(text = stringResource(R.string.login_label))
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun SignUpScreenPreview() {
    MobileDevTheme {
        SignUpScreen(
            uiState = SignUpUiState(),
            onEvent = {},
            onLoginClick = {}
        )
    }
}

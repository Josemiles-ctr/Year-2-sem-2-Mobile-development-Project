package com.example.mobiledev.feature.signup.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mobiledev.R
import com.example.mobiledev.ui.components.AuthInputField
import com.example.mobiledev.ui.components.BrandHeader
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
                SignUpViewModel.NavigationEvent.NavigateToDashboard -> onAuthSuccess()
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
                .imePadding()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BrandHeader()
            SignUpFormCard(uiState = uiState, onEvent = onEvent, onLoginClick = onLoginClick)
        }
    }
}

@Composable
private fun SignUpFormCard(
    uiState: SignUpUiState,
    onEvent: (SignUpEvent) -> Unit,
    onLoginClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.24f)
                ),
                MaterialTheme.shapes.extraLarge
            ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.42f)
        )
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
                modifier = Modifier.testTag("signup_title"),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            AuthInputField(
                value = uiState.fullName,
                onValueChange = { onEvent(SignUpEvent.FullNameChanged(it)) },
                label = stringResource(R.string.full_name_label),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("signup_full_name_input")
            )

            AuthInputField(
                value = uiState.phoneNumber,
                onValueChange = { onEvent(SignUpEvent.PhoneNumberChanged(it)) },
                label = stringResource(R.string.phone_number_label),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("signup_phone_input")
            )

            AuthInputField(
                value = uiState.email,
                onValueChange = { onEvent(SignUpEvent.EmailChanged(it)) },
                label = stringResource(R.string.email_label),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("signup_email_input")
            )

            AuthInputField(
                value = uiState.password,
                onValueChange = { onEvent(SignUpEvent.PasswordChanged(it)) },
                label = stringResource(R.string.password_label),
                isPassword = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("signup_password_input")
            )

            AuthInputField(
                value = uiState.confirmPassword,
                onValueChange = { onEvent(SignUpEvent.ConfirmPasswordChanged(it)) },
                label = stringResource(R.string.confirm_password_label),
                isPassword = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("signup_confirm_password_input")
            )

            val feedback = uiState.errorMessage
            if (!feedback.isNullOrBlank()) {
                Text(
                    text = feedback,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Button(
                onClick = { onEvent(SignUpEvent.Submit) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("signup_submit_button")
            ) {
                Text(text = stringResource(R.string.create_account_label))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.already_have_account_label),
                    modifier = Modifier.testTag("signup_already_have_account_text"),
                    style = MaterialTheme.typography.bodySmall
                )
                TextButton(
                    onClick = onLoginClick,
                    modifier = Modifier.testTag("signup_signin_button")
                ) {
                    Text(text = stringResource(R.string.sign_in_cta_label))
                }
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

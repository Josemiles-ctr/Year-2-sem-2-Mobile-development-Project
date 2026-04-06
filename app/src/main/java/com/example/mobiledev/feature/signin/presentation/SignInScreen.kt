package com.example.mobiledev.feature.signin.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mobiledev.R
import com.example.mobiledev.ui.components.AuthInputField
import com.example.mobiledev.ui.components.BrandHeader
import com.example.mobiledev.ui.components.AuthScreenContainer
import com.example.mobiledev.ui.theme.MobileDevTheme
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SignInRoute(
    viewModel: SignInViewModel,
    onSignUpClick: () -> Unit = {},
    onAuthSuccess: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.navigationEvents.collectLatest { event ->
            when (event) {
                SignInViewModel.NavigationEvent.NavigateToDashboard -> onAuthSuccess()
            }
        }
    }

    SignInScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onSignUpClick = onSignUpClick
    )
}

@Composable
fun SignInScreen(
    uiState: SignInUiState,
    onEvent: (SignInEvent) -> Unit,
    onSignUpClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AuthScreenContainer(modifier = modifier) {
        BrandHeader()

        LoginFormCard(
            uiState = uiState,
            onEvent = onEvent
        )

        SignUpPromptCard(onSignUpClick = onSignUpClick)
    }
}

@Composable
private fun LoginFormCard(
    uiState: SignInUiState,
    onEvent: (SignInEvent) -> Unit
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
                text = stringResource(R.string.login_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            AuthInputField(
                value = uiState.emailOrPhone,
                onValueChange = { onEvent(SignInEvent.EmailOrPhoneChanged(it)) },
                label = stringResource(R.string.email_or_phone_label),
                modifier = Modifier.fillMaxWidth()
            )

            AuthInputField(
                value = uiState.password,
                onValueChange = { onEvent(SignInEvent.PasswordChanged(it)) },
                label = stringResource(R.string.password_hint_label),
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
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
                onClick = { onEvent(SignInEvent.Submit) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(text = stringResource(R.string.login_label))
            }
        }
    }
}

@Composable
private fun SignUpPromptCard(onSignUpClick: () -> Unit) {
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
                text = stringResource(R.string.no_account_label),
                style = MaterialTheme.typography.bodySmall
            )
            OutlinedButton(onClick = onSignUpClick) {
                Text(text = stringResource(R.string.sign_up_label))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SignInScreenPreview() {
    MobileDevTheme {
        SignInScreen(
            uiState = SignInUiState(),
            onEvent = {},
            onSignUpClick = {}
        )
    }
}

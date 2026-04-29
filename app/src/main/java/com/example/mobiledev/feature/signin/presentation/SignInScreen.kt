package com.example.mobiledev.feature.signin.presentation

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.remember
import com.example.mobiledev.core.error.AppError
import com.example.mobiledev.core.error.toUserMessage
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SignInRoute(
    viewModel: SignInViewModel,
    onSignUpClick: () -> Unit = {},
    onAuthSuccess: () -> Unit = {},
    onHospitalSignInClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val appError by viewModel.appError.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.navigationEvents.collectLatest { event ->
            when (event) {
                SignInViewModel.NavigationEvent.NavigateToDashboard -> onAuthSuccess()
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.successMessage.collectLatest { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(appError) {
        appError?.let { error ->
            val message = error.toUserMessage(context)

            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = if (error is AppError.NetworkError || error is AppError.NoInternetError) {
                    val retryActionLabel = "Retry"
                    retryActionLabel
                } else null
            )

            if (result == SnackbarResult.ActionPerformed) {
                viewModel.onEvent(SignInEvent.Submit)
            }
            viewModel.clearError()
        }
    }

    SignInScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent,
        onSignUpClick = onSignUpClick,
        onHospitalSignInClick = onHospitalSignInClick
    )
}

@Composable
fun SignInScreen(
    uiState: SignInUiState,
    snackbarHostState: SnackbarHostState,
    onEvent: (SignInEvent) -> Unit,
    onSignUpClick: () -> Unit,
    onHospitalSignInClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent, // Ensure background shows through
        modifier = modifier
    ) { padding ->
        AuthScreenContainer(modifier = Modifier.padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BrandHeader()

                LoginFormCard(
                    uiState = uiState,
                    onEvent = onEvent,
                    onSignUpClick = onSignUpClick,
                    onHospitalSignInClick = onHospitalSignInClick
                )
            }
        }
    }
}

@Composable
private fun LoginFormCard(
    uiState: SignInUiState,
    onEvent: (SignInEvent) -> Unit,
    onSignUpClick: () -> Unit,
    onHospitalSignInClick: () -> Unit
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
                text = stringResource(R.string.login_title),
                modifier = Modifier.testTag("signin_title"),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            AuthInputField(
                value = uiState.emailOrPhone,
                onValueChange = { onEvent(SignInEvent.EmailOrPhoneChanged(it)) },
                label = stringResource(R.string.email_or_phone_label),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("signin_email_or_phone_input")
            )

            AuthInputField(
                value = uiState.password,
                onValueChange = { onEvent(SignInEvent.PasswordChanged(it)) },
                label = stringResource(R.string.password_hint_label),
                isPassword = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("signin_password_input")
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
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("signin_submit_button")
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(22.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(text = stringResource(R.string.sign_in_cta_label))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.no_account_label),
                    modifier = Modifier.testTag("signin_no_account_text"),
                    style = MaterialTheme.typography.bodySmall
                )
                TextButton(
                    onClick = onSignUpClick,
                    enabled = !uiState.isLoading,
                    modifier = Modifier.testTag("signin_signup_button")
                ) {
                    Text(text = stringResource(R.string.sign_up_label))
                }
            }

            TextButton(
                onClick = onHospitalSignInClick,
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Hospital Admin? Login here",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignInScreenPreview() {
    MobileDevTheme {
        SignInScreen(
            uiState = SignInUiState(),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
            onSignUpClick = {},
            onHospitalSignInClick = {}
        )
    }
}

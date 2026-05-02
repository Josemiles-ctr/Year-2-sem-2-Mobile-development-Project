package com.example.mobiledev.feature.signup.presentation

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobiledev.R
import com.example.mobiledev.core.error.AppError
import com.example.mobiledev.core.error.toUserMessage
import com.example.mobiledev.ui.components.AuthInputField
import com.example.mobiledev.ui.components.BrandHeader
import com.example.mobiledev.ui.components.CompactLoadingIndicator
import com.example.mobiledev.ui.theme.MobileDevTheme
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SignUpRoute(
    viewModel: SignUpViewModel,
    onLoginClick: () -> Unit = {},
    onAuthSuccess: () -> Unit = {}
) {
    val uiState by viewModel.signUpUiState.collectAsState()
    val appError by viewModel.appError.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.navigationEvents.collectLatest { event ->
            when (event) {
                SignUpViewModel.NavigationEvent.NavigateToDashboard -> onAuthSuccess()
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
                    context.getString(R.string.action_retry)
                } else null
            )

            if (result == SnackbarResult.ActionPerformed) {
                viewModel.onEvent(SignUpEvent.Submit)
            }
            viewModel.clearError()
        }
    }

    SignUpScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent,
        onLoginClick = onLoginClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    uiState: SignUpUiState,
    snackbarHostState: SnackbarHostState,
    onEvent: (SignUpEvent) -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFFBFBFB),
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            BrandHeader()

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.create_account_label),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333)
                )
            )
            
            Text(
                text = stringResource(R.string.access_emergency_dashboard),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFF666666)
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            SignUpForm(
                uiState = uiState,
                onEvent = onEvent,
                onLoginClick = onLoginClick
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Image Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.8f)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.splash_screen),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.active_emergency_network),
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                    )
                    Text(
                        text = stringResource(R.string.trusted_hospitals),
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Text(
                text = stringResource(R.string.function_first_reliability),
                style = MaterialTheme.typography.labelLarge.copy(
                    color = Color.Gray,
                    letterSpacing = 1.5.sp
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = stringResource(R.string.privacy_policy),
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                )
                Text(
                    text = stringResource(R.string.terms_of_service),
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                )
            }
        }
    }
}

@Composable
private fun SignUpForm(
    uiState: SignUpUiState,
    onEvent: (SignUpEvent) -> Unit,
    onLoginClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AuthInputField(
            value = uiState.fullName,
            onValueChange = { onEvent(SignUpEvent.FullNameChanged(it)) },
            label = stringResource(R.string.full_name_label),
            leadingIcon = Icons.Default.Person,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("signup_full_name_input")
        )

        AuthInputField(
            value = uiState.phoneNumber,
            onValueChange = { onEvent(SignUpEvent.PhoneNumberChanged(it)) },
            label = stringResource(R.string.phone_number_label),
            leadingIcon = Icons.Default.Phone,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("signup_phone_input")
        )

        AuthInputField(
            value = uiState.email,
            onValueChange = { onEvent(SignUpEvent.EmailChanged(it)) },
            label = stringResource(R.string.email_label),
            leadingIcon = Icons.Default.Email,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("signup_email_input")
        )

        AuthInputField(
            value = uiState.password,
            onValueChange = { onEvent(SignUpEvent.PasswordChanged(it)) },
            label = stringResource(R.string.password_label),
            isPassword = true,
            leadingIcon = Icons.Default.Lock,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("signup_password_input")
        )

        AuthInputField(
            value = uiState.confirmPassword,
            onValueChange = { onEvent(SignUpEvent.ConfirmPasswordChanged(it)) },
            label = stringResource(R.string.confirm_password_label),
            isPassword = true,
            leadingIcon = Icons.Default.Lock,
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

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { onEvent(SignUpEvent.Submit) },
            enabled = !uiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("signup_submit_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFC61111)
            )
        ) {
            if (uiState.isLoading) {
                CompactLoadingIndicator(color = Color.White)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.create_account_label),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.already_have_account_label),
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
            )
            TextButton(
                onClick = onLoginClick,
                enabled = !uiState.isLoading
            ) {
                Text(
                    text = stringResource(R.string.sign_in_cta_label),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF00796B),
                        fontWeight = FontWeight.Bold
                    )
                )
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
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
            onLoginClick = {}
        )
    }
}

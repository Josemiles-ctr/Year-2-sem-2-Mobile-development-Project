package com.example.mobiledev.feature.signup.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mobiledev.R
import com.example.mobiledev.ui.theme.MobileDevTheme

@Composable
fun SignUpRoute(
    viewModel: SignUpViewModel,
    onLoginClick: () -> Unit = {}
) {
    val uiState by viewModel.signUpUiState.collectAsState()

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
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.sign_up_title),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(28.dp))

        SignUpTextField(
            value = uiState.fullName,
            onValueChange = { onEvent(SignUpEvent.FullNameChanged(it)) },
            placeholder = stringResource(R.string.full_name_label)
        )
        Spacer(modifier = Modifier.height(12.dp))

        SignUpTextField(
            value = uiState.phoneNumber,
            onValueChange = { onEvent(SignUpEvent.PhoneNumberChanged(it)) },
            placeholder = stringResource(R.string.phone_number_label)
        )
        Spacer(modifier = Modifier.height(12.dp))

        SignUpTextField(
            value = uiState.email,
            onValueChange = { onEvent(SignUpEvent.EmailChanged(it)) },
            placeholder = stringResource(R.string.email_label)
        )
        Spacer(modifier = Modifier.height(12.dp))

        SignUpTextField(
            value = uiState.password,
            onValueChange = { onEvent(SignUpEvent.PasswordChanged(it)) },
            placeholder = stringResource(R.string.password_label),
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(12.dp))

        SignUpTextField(
            value = uiState.confirmPassword,
            onValueChange = { onEvent(SignUpEvent.ConfirmPasswordChanged(it)) },
            placeholder = stringResource(R.string.confirm_password_label),
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(16.dp))

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
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = { onEvent(SignUpEvent.Submit) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.create_account_label))
        }
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.already_have_account_label),
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onLoginClick,
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text(text = stringResource(R.string.login_label))
        }
    }
}

@Composable
private fun SignUpTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder) },
        visualTransformation = visualTransformation,
        singleLine = true
    )
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





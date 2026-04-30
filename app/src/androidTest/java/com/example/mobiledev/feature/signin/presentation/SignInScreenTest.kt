package com.example.mobiledev.feature.signin.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import com.example.mobiledev.ui.theme.MobileDevTheme
import org.junit.Rule
import org.junit.Test

class SignInScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun signInScreen_showsMainFieldsAndActions() {
        composeTestRule.setContent {
            MobileDevTheme {
                SignInScreen(
                    uiState = SignInUiState(),
                    snackbarHostState = SnackbarHostState(),
                    onEvent = {},
                    onSignUpClick = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("signin_title").assertIsDisplayed()
        composeTestRule.onNodeWithTag("signin_email_or_phone_input").assertIsDisplayed()
        composeTestRule.onNodeWithTag("signin_password_input").assertIsDisplayed()
        composeTestRule.onNodeWithTag("signin_no_account_text").assertIsDisplayed()
        composeTestRule.onNodeWithTag("signin_submit_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("signin_signup_button").assertIsDisplayed()
    }
}

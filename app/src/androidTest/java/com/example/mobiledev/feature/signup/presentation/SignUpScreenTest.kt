package com.example.mobiledev.feature.signup.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.example.mobiledev.ui.theme.MobileDevTheme
import org.junit.Rule
import org.junit.Test

class SignUpScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun signUpScreen_showsAllMainFieldsAndActions() {
        composeTestRule.setContent {
            MobileDevTheme {
                SignUpScreen(
                    uiState = SignUpUiState(),
                    onEvent = {},
                    onLoginClick = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("signup_title").assertIsDisplayed()
        composeTestRule.onNodeWithTag("signup_full_name_input").assertIsDisplayed()
        composeTestRule.onNodeWithTag("signup_phone_input").assertIsDisplayed()
        composeTestRule.onNodeWithTag("signup_email_input").assertIsDisplayed()
        composeTestRule.onNodeWithTag("signup_password_input").assertIsDisplayed()
        composeTestRule.onNodeWithTag("signup_confirm_password_input").assertIsDisplayed()
        composeTestRule.onNodeWithTag("signup_submit_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("signup_signin_button").assertIsDisplayed()
    }
}



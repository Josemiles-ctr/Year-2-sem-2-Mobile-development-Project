package com.example.mobiledev.feature.signin.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
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
                    onEvent = {},
                    onSignUpClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Login").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email / Phone number").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password:").assertIsDisplayed()
        composeTestRule.onNodeWithText("Do not have an account?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sign In").assertIsDisplayed()
    }
}

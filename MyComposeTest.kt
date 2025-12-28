package com.example.menuannam

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.menuannam.ui.theme.MenuAnNamTheme
import org.junit.Rule
import org.junit.Test

class MyComposeTest {

    @get:Rule
    val composeTestRule = createComposeRule()
    // use createAndroidComposeRule<YourActivity>() if you need access to
    // an activity

    @Test
    fun myTest() {
        // Start the app
        composeTestRule.setContent {
            MenuAnNamTheme {
                AppNavigation()
            }
        }


        composeTestRule.onNodeWithText("Study").assertIsDisplayed()
    }
}
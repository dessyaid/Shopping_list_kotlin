package com.example.shoppinglist

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Rule


@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun addItemTest() {
        composeTestRule.onNodeWithText("Enter item").performTextInput("Milk")
        composeTestRule.onNodeWithText("Add").performClick()
        composeTestRule.onNodeWithText("Enter item").assertExists()
    }

    @Test
    fun deleteItemTest() {
        composeTestRule.onNodeWithText("Enter item").performTextInput("Bread")
        composeTestRule.onNodeWithText("Add").performClick()

        composeTestRule.onAllNodesWithContentDescription("Delete")[0].performClick()

        composeTestRule.onNodeWithText("Bread").assertDoesNotExist()
    }

    @Test
    fun undoDeleteTest() {
        composeTestRule.onNodeWithText("Enter item").performTextInput("Tomato")
        composeTestRule.onNodeWithText("Add").performClick()

        composeTestRule.onAllNodesWithContentDescription("Delete")[0].performClick()
        composeTestRule.onNodeWithText("Cancel").performClick()

        composeTestRule.onNodeWithText("Tomato").assertExists()
    }
}
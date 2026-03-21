package com.example.shoppinglist

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
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
        composeTestRule.onNodeWithText("Add Item").performTextInput("Milk")
        composeTestRule.onNodeWithText("Add").performClick()
        composeTestRule.onNodeWithText("Milk").assertExists()
        composeTestRule.onAllNodesWithContentDescription("Delete")[0].performClick()
    }

    @Test
    fun deleteItemTest() {
        composeTestRule.onNodeWithText("Add Item").performTextInput("Bread")
        composeTestRule.onNodeWithText("Add").performClick()

        composeTestRule.onAllNodesWithContentDescription("Delete")[0].performClick()

        composeTestRule.onNodeWithText("Bread").assertDoesNotExist()
    }

    @Test
    fun undoDeleteTest() {
        composeTestRule.onNodeWithText("Add Item").performTextInput("Tomato")
        composeTestRule.onNodeWithText("Add").performClick()

        composeTestRule.onAllNodesWithContentDescription("Delete")[0].performClick()
        composeTestRule.onNodeWithText("Cancel").performClick()

        composeTestRule.onNodeWithText("Tomato").assertExists()
        composeTestRule.onAllNodesWithContentDescription("Delete")[0].performClick()
    }

    @Test
    fun boughtTest() {
        val itemName = "Cheese"
        composeTestRule.onNodeWithText("Add Item").performTextInput(itemName)
        composeTestRule.onNodeWithText("Add").performClick()

        composeTestRule.onNode(isToggleable()).performClick()

        composeTestRule.onNode(isToggleable()).assertIsOn()

        composeTestRule.onAllNodesWithContentDescription("Delete")[0].performClick()
    }
}
package com.example.budgiet

import android.os.SystemClock.sleep
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test

class TransactionTests {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun showsPlaceHolderValueOnPriceField() {
        composeTestRule.setContent {
            MainPage()
        }

        composeTestRule.onNode(
            hasContentDescription("New Transaction")
                    and
                    hasClickAction()
        ).performClick()

        // Note: includeEditableText should be marked as false
        // because we're only checking that the placeholder value
        // is seen (Text = "0", EditableText = "")
        composeTestRule
            .onNodeWithTag("price_input_field")
            .assertTextEquals("0", includeEditableText = false) // placeholder text should be seen
    }

    @Test
    fun enterWholeIntegerOnPriceField() {
        composeTestRule.setContent {
            MainPage()
        }

        composeTestRule.onNode(
            hasContentDescription("New Transaction")
                    and
                    hasClickAction()
        ).performClick()

        composeTestRule
            .onNodeWithTag("price_input_field")
            .performTextInput("100")

        composeTestRule
            .onNodeWithTag("price_input_field")
            .assertTextEquals("100")
    }

    @Test
    fun enterDecimalOnPriceField() {
        composeTestRule.setContent {
            MainPage()
        }

        composeTestRule.onNode(
            hasContentDescription("New Transaction")
                    and
                    hasClickAction()
        ).performClick()

        composeTestRule
            .onNodeWithTag("price_input_field")
            .performTextInput("100.00")

        composeTestRule
            .onNodeWithTag("price_input_field")
            .assertTextEquals("100.00")
    }

    @Test
    fun enterInvalidCharacterOnPriceField() {
        composeTestRule.setContent {
            MainPage()
        }

        composeTestRule.onNode(
            hasContentDescription("New Transaction")
                    and
                    hasClickAction()
        ).performClick()

        composeTestRule
            .onNodeWithTag("price_input_field")
            .performTextInput("f")

        // TODO: This needs a screenshot UI test to see a red outline on the box
        composeTestRule
            .onNodeWithTag("price_input_field")
            .assertTextEquals("f") // placeholder text should be seen
    }

    @Test
    fun enterHalfValidHalfInvalidCharactersPriceField() {
        composeTestRule.setContent {
            MainPage()
        }

        composeTestRule.onNode(
            hasContentDescription("New Transaction")
                    and
                    hasClickAction()
        ).performClick()

        composeTestRule
            .onNodeWithTag("price_input_field")
            .performTextInput("1")

        composeTestRule
            .onNodeWithTag("price_input_field")
            .assertTextEquals("1")

        composeTestRule
            .onNodeWithTag("price_input_field")
            .performTextInput("f")

        // TODO: This needs a screenshot UI test to see a red outline on the box
        composeTestRule
            .onNodeWithTag("price_input_field")
            .assertTextEquals("1f")
    }

}
package com.example.budgiet

import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test

class TransactionTests {

    @get:Rule
    val composeTestRule = createComposeRule()
    val price_input_node = composeTestRule
        .onNodeWithTag("price_input_field")

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
        price_input_node
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

        price_input_node
            .performTextInput("100")

        price_input_node
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

        price_input_node
            .performTextInput("100.00")

        price_input_node
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

        price_input_node
            .performTextInput("f")


        // TODO: This needs a screenshot UI test to see a red outline on the box
        price_input_node
            .assertTextContains("f")

        price_input_node
            .assertTextEquals("f is not a valid price value", includeEditableText = false)
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

        price_input_node
            .performTextInput("1")

        price_input_node
            .assertTextEquals("1")

        price_input_node
            .performTextInput("f")

        // TODO: This needs a screenshot UI test to see a red outline on the box
        price_input_node
            .assertTextContains("1f")

        price_input_node
            .assertTextEquals("1f is not a valid price value", includeEditableText = false)
    }

}
package com.example.budgiet.transactionTests

import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.budgiet.MainPage
import org.junit.Rule
import org.junit.Test

class PriceFieldTests {
    @get:Rule
    val composeTestRule = createComposeRule()
    val priceInputNode = composeTestRule
        .onNodeWithTag("price_input_field")

    @Test
    fun showsPlaceHolderValue() {
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
        priceInputNode
            .assertTextEquals("0", includeEditableText = false) // placeholder text should be seen
    }

    @Test
    fun enterWholeInteger() {
        composeTestRule.setContent {
            MainPage()
        }

        composeTestRule.onNode(
            hasContentDescription("New Transaction")
                    and
                    hasClickAction()
        ).performClick()

        priceInputNode
            .performTextInput("100")

        priceInputNode
            .assertTextEquals("100")
    }

    @Test
    fun enterDecimal() {
        composeTestRule.setContent {
            MainPage()
        }

        composeTestRule.onNode(
            hasContentDescription("New Transaction")
                    and
                    hasClickAction()
        ).performClick()

        priceInputNode
            .performTextInput("100.00")

        priceInputNode
            .assertTextEquals("100.00")
    }

    @Test
    fun enterInvalidCharacter() {
        composeTestRule.setContent {
            MainPage()
        }

        composeTestRule.onNode(
            hasContentDescription("New Transaction")
                    and
                    hasClickAction()
        ).performClick()

        priceInputNode
            .performTextInput("f")


        // TODO: This needs a screenshot UI test to see a red outline on the box
        priceInputNode
            .assertTextContains("f")

        priceInputNode
            .assertTextEquals("f is not a valid price value", includeEditableText = false)
    }

    @Test
    fun enterHalfValidHalfInvalidCharacters() {
        composeTestRule.setContent {
            MainPage()
        }

        composeTestRule.onNode(
            hasContentDescription("New Transaction")
                    and
                    hasClickAction()
        ).performClick()

        priceInputNode
            .performTextInput("1")

        priceInputNode
            .assertTextEquals("1")

        priceInputNode
            .performTextInput("f")

        // TODO: This needs a screenshot UI test to see a red outline on the box
        priceInputNode
            .assertTextContains("1f")

        priceInputNode
            .assertTextEquals("1f is not a valid price value", includeEditableText = false)
    }

}
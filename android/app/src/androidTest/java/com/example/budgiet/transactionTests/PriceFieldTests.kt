package com.example.budgiet.transactionTests

import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.requestFocus
import com.example.budgiet.MainPage
import org.junit.Rule
import org.junit.Test

class PriceFieldTests {
    @get:Rule
    val composeTestRule = createComposeRule()
    val priceInputNode = composeTestRule
        .onNodeWithTag("price_input_field")

    // This node is used to re-direct focus to see if
    // number formatting is done
    val dateTextNode = composeTestRule
        .onNodeWithTag(
            "DateTextField"
        )

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

        priceInputNode.assertIsFocused()

        priceInputNode
            .assertTextEquals("100")

        dateTextNode.performClick()

        priceInputNode.assertIsNotFocused()

        priceInputNode
            .assertTextEquals("100.00")
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

        priceInputNode.assertIsFocused()

        priceInputNode
            .assertTextEquals("100.00")

        dateTextNode.performClick()

        priceInputNode.assertIsNotFocused()

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

        priceInputNode.assertIsFocused()

        priceInputNode
            .assertTextEquals("f")

        dateTextNode.performClick()

        priceInputNode.assertIsNotFocused()

        priceInputNode
            .assertTextEquals("Invalid character 'f' used", includeEditableText = false)
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

        priceInputNode.assertIsFocused()

        priceInputNode
            .assertTextEquals("1")

        priceInputNode
            .performTextInput("f")

        priceInputNode.assertIsFocused()

        // TODO: This needs a screenshot UI test to see a red outline on the box
        priceInputNode
            .assertTextContains("1f")

        dateTextNode.performClick()

        priceInputNode.assertIsNotFocused()

        priceInputNode
            .assertTextEquals("Invalid character 'f' used", includeEditableText = false)
    }

    @Test
    fun enterLeadingUnfractionalZero() {
        composeTestRule.setContent {
            MainPage()
        }

        composeTestRule.onNode(
            hasContentDescription("New Transaction")
                    and
                    hasClickAction()
        ).performClick()

        priceInputNode
            .performTextInput("01")


        // TODO: This needs a screenshot UI test to see a red outline on the box

        priceInputNode.assertIsFocused()

        priceInputNode
            .assertTextEquals("01")

        dateTextNode.performClick()

        priceInputNode.assertIsNotFocused()

        priceInputNode
            .assertTextEquals("Leading un-fractional 0s are not allowed", includeEditableText = false)
    }

    @Test
    fun enterManyDecimals() {
        composeTestRule.setContent {
            MainPage()
        }

        composeTestRule.onNode(
            hasContentDescription("New Transaction")
                    and
                    hasClickAction()
        ).performClick()

        priceInputNode
            .performTextInput("1..0")


        // TODO: This needs a screenshot UI test to see a red outline on the box

        priceInputNode.assertIsFocused()

        priceInputNode
            .assertTextEquals("1..0")

        dateTextNode.performClick()

        priceInputNode.assertIsNotFocused()

        priceInputNode
            .assertTextEquals("Decimal '.' exists already", includeEditableText = false)
    }

    @Test
    fun enterTooManyDecimalPlaces() {
        composeTestRule.setContent {
            MainPage()
        }

        composeTestRule.onNode(
            hasContentDescription("New Transaction")
                    and
                    hasClickAction()
        ).performClick()

        priceInputNode
            .performTextInput("1.000")


        // TODO: This needs a screenshot UI test to see a red outline on the box

        priceInputNode.assertIsFocused()

        priceInputNode
            .assertTextEquals("1.000")

        dateTextNode.performClick()

        priceInputNode.assertIsNotFocused()

        priceInputNode
            .assertTextEquals("USD uses up to 2 decimal places", includeEditableText = false)
    }

    @Test
    fun enterInvalidDecimalSymbol() {
        composeTestRule.setContent {
            MainPage()
        }

        composeTestRule.onNode(
            hasContentDescription("New Transaction")
                    and
                    hasClickAction()
        ).performClick()

        priceInputNode
            .performTextInput("1,00")


        // TODO: This needs a screenshot UI test to see a red outline on the box

        priceInputNode.assertIsFocused()

        priceInputNode
            .assertTextEquals("1,00")

        dateTextNode.performClick()

        priceInputNode.assertIsNotFocused()

        priceInputNode
            .assertTextEquals("Your locale uses '.' as decimal", includeEditableText = false)
    }
}
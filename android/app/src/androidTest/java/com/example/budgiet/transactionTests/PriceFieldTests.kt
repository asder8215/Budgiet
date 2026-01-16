package com.example.budgiet.transactionTests

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.budgiet.ui.PriceField
import org.junit.Rule
import org.junit.Test

class PriceFieldTests {
    @get:Rule
    val composeTestRule = createComposeRule()
    val priceInputNode
        get() = composeTestRule
            .onNodeWithTag("priceInputField")

    // This node is used to re-direct focus to see if
    // number formatting is done
    val buttonNode
        get() = composeTestRule
            .onNodeWithTag("button")


    fun priceContent(selectedPrice: MutableState<String>) {
        composeTestRule.setContent {
            val focusManager = LocalFocusManager.current
            Column {
                PriceField(
                    modifier = Modifier.testTag("priceInputField"),
                    initialPrice = selectedPrice.value,
                    onPriceChange = { selectedPrice.value = it }
                )
                Button(
                    modifier = Modifier.testTag("button"),
                    onClick = { focusManager.clearFocus() },
                    content = {},
                )
            }
        }
    }

    data class PriceAssertion(
        val text: String?,
        val onFocusAssertion: String?,
        val onUnfocusAssertion: String?,
        val includeEditableText: Boolean
    )

    fun priceTestCase(
        assertions: List<PriceAssertion>,
    ) {

        assertions.forEach { assertion ->

            if (assertion.text != null) {
                priceInputNode
                    .performTextInput(assertion.text)
            }

            if (assertion.onFocusAssertion != null) {
                priceInputNode.assertIsFocused()
                priceInputNode
                    .assertTextEquals(
                        assertion.onFocusAssertion,
                        includeEditableText = assertion.includeEditableText
                    )
            }

            if (assertion.onUnfocusAssertion != null) {
                buttonNode.performClick()
                priceInputNode.assertIsNotFocused()
                priceInputNode
                    .assertTextEquals(
                        assertion.onUnfocusAssertion,
                        includeEditableText = assertion.includeEditableText
                    )
            }

        }

    }

    @Test
    fun showsPlaceHolderValue() {
        val selectedPrice = mutableStateOf("")

        priceContent(selectedPrice)

        // Note: includeEditableText should be marked as false
        // because we're only checking that the placeholder value
        // is seen (Text = "0", EditableText = "")
        priceInputNode
            .assertTextEquals("0", includeEditableText = false) // placeholder text should be seen
    }

    @Test
    fun enterWholeInteger() {
        val selectedPrice = mutableStateOf("")

        priceContent(selectedPrice)

        priceTestCase(
            listOf(
                PriceAssertion(
                    "100",
                    "100",
                    null,
                    true
                ),
                PriceAssertion(
                    null,
                    null,
                    "100.00",
                    true
                )
            )
        )
    }

    @Test
    fun enterDecimal() {
        val selectedPrice = mutableStateOf("")

        priceContent(selectedPrice)

        priceTestCase(
            listOf(
                PriceAssertion(
                    "100.00",
                    "100.00",
                    null,
                    true
                ),
                PriceAssertion(
                    null,
                    null,
                    "100.00",
                    true
                )
            )
        )
    }

    @Test
    fun enterInvalidCharacter() {
        val selectedPrice = mutableStateOf("")

        priceContent(selectedPrice)

        priceTestCase(
            listOf(
                PriceAssertion(
                    "f",
                    "f",
                    null,
                    true
                ),
                PriceAssertion(
                    null,
                    null,
                    "Invalid character 'f' used",
                    false
                )
            )
        )
    }

    @Test
    fun enterHalfValidHalfInvalidCharacters() {
        val selectedPrice = mutableStateOf("")

        priceContent(selectedPrice)

        priceTestCase(
            listOf(
                PriceAssertion(
                    "1",
                    "1",
                    null,
                    true
                ),
                PriceAssertion(
                    "f",
                    "1f",
                    null,
                    true
                ),
                PriceAssertion(
                    null,
                    null,
                    "Invalid character 'f' used",
                    false
                )
            )
        )
    }

    @Test
    fun enterLeadingUnfractionalZero() {
        val selectedPrice = mutableStateOf("")

        priceContent(selectedPrice)

        priceTestCase(
            listOf(
                PriceAssertion(
                    "01",
                    "01",
                    null,
                    true
                ),
                PriceAssertion(
                    null,
                    null,
                    "Leading un-fractional 0s are not allowed",
                    false
                )
            )
        )
    }

    @Test
    fun enterManyDecimals() {
        val selectedPrice = mutableStateOf("")

        priceContent(selectedPrice)

        priceTestCase(
            listOf(
                PriceAssertion(
                    "1..0",
                    "1..0",
                    null,
                    true
                ),
                PriceAssertion(
                    null,
                    null,
                    "Decimal '.' exists already",
                    false
                )
            )
        )
    }

    @Test
    fun enterTooManyDecimalPlaces() {
        val selectedPrice = mutableStateOf("")

        priceContent(selectedPrice)

        priceTestCase(
            listOf(
                PriceAssertion(
                    "1.000",
                    "1.000",
                    null,
                    true
                ),
                PriceAssertion(
                    null,
                    null,
                    "USD uses up to 2 decimal places",
                    false
                )
            )
        )
    }

    @Test
    fun enterInvalidDecimalSymbol() {
        val selectedPrice = mutableStateOf("")

        priceContent(selectedPrice)

        priceTestCase(
            listOf(
                PriceAssertion(
                    "1,00",
                    "1,00",
                    null,
                    true
                ),
                PriceAssertion(
                    null,
                    null,
                    "Your locale uses '.' as decimal",
                    false
                )
            )
        )
    }
}
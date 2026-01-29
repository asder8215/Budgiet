package com.example.budgiet.transactionTests

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import com.example.budgiet.getSemanticsProperty
import com.example.budgiet.ui.DESCRIPTION_MAX_LENGTH
import com.example.budgiet.ui.DescriptionField
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

const val DESCRIPTION_FIELD_TAG = "DescriptionField"

private class TestState(private val rule: ComposeContentTestRule) {

    val descriptionField
        get() = this.rule.onNodeWithTag(DESCRIPTION_FIELD_TAG)

    init {
        rule.setContent {
            var fieldValue by remember { mutableStateOf("") }
            DescriptionField(
                modifier = Modifier.testTag(DESCRIPTION_FIELD_TAG),
                fieldValue = fieldValue,
                onValueChange = { fieldValue = it },
            )
        }
    }
}

class DescriptionFieldTests {
    @get:Rule
    val rule = createComposeRule()

    // TODO: test graphemes with multiple codepoints (e.g. ä (are you sure this is the multi-codepoint variant?), certain emojis)
    // TODO: test pasting more than 255 characters

    @Test
    fun typingLimitTest() {
        val state = TestState(rule)

        /** Assert that the **character counter** Node is at a certain number. */
        fun assertCounter(count: Int) = assertEquals(
            "$count/$DESCRIPTION_MAX_LENGTH",
            // The counter Node is always the last in the TextField Node.
            state.descriptionField.getSemanticsProperty(SemanticsProperties.Text).last().text
        )

        // Check that counter starts at 0.
        assertCounter(0)

        // Type ASCII characters up to the limit.
        (0..DESCRIPTION_MAX_LENGTH).forEach { _ ->
            state.descriptionField.performTextInput("a")
        }

        assertEquals(
            "a".repeat(DESCRIPTION_MAX_LENGTH),
            state.descriptionField.getSemanticsProperty(SemanticsProperties.EditableText).text,
        )

        // Check that the counter is updated.
        assertCounter(DESCRIPTION_MAX_LENGTH)

        // Type one more ASCII character, check that it is ignored/blocked.
        state.descriptionField.performTextInput("b")
        assert(!state.descriptionField.getSemanticsProperty(SemanticsProperties.EditableText).contains('b'))
    }
}

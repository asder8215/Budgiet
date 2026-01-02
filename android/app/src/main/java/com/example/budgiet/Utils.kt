package com.example.budgiet

import android.annotation.SuppressLint
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlainToolTipBox(
    text: String,
    modifier: Modifier = Modifier,
    content: @Composable (() -> Unit)
) {
    TooltipBox(
        modifier = modifier,
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        state = rememberTooltipState(),
        tooltip = {
            PlainTooltip { Text(text) }
        },
        content = content,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlainSearchBar(
    modifier: Modifier = Modifier,
//    expandable: Boolean = false,
    onQueryChange: (CharSequence) -> Unit,
    state: TextFieldState = rememberTextFieldState(),
) {
//    var expanded by remember { mutableStateOf(false) }
//    val onExpandedChange = { new: Boolean ->
//        expanded = new && expandable
//    }

    DockedSearchBar(
        modifier = modifier,
        expanded = false,
        onExpandedChange = { },
        colors = SearchBarDefaults.colors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        inputField = {
            SearchBarDefaults.InputField(
                query = state.text.toString(),
                onQueryChange = {
                    state.edit { replace(0, length, it) }
                    onQueryChange(state.text)
                },
                onSearch = { },
                expanded = false,
                onExpandedChange = { },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    unfocusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                placeholder = { Text("Search existing locations") },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                trailingIcon = if (state.text.isNotEmpty()) { {
                    PlainToolTipBox("Clear search") {
                        IconButton(onClick = { state.edit { replace(0, length, "") } }) {
                            Icon(Icons.Filled.Clear, "Clear search")
                        }
                    }
                } } else {
                    null
                },
            )
        }
    ) { }
}

// Formatting method taken from https://stackoverflow.com/a/56668796/32115191.
// According to the answer, the java.time package can be used in any version of Android, so the warning can be suppressed.
@SuppressLint("NewApi")
class Date private constructor(private val localDate: LocalDate) {
    constructor(millis: Long) : this(
        java.util.Date(millis).let { dateMillis ->
            LocalDate.of(dateMillis.year, dateMillis.month, dateMillis.date)
        }
    )

    override fun toString(): String {
        val now = LocalDate.now()
        println("localDate = $localDate")
        println("now = $now")

        return if (localDate == now) {
            "Today"
        } else if (localDate == now.minusDays(1)) {
            "Yesterday"
        } else {
            val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
            localDate.format(formatter)
        }
    }

    companion object {
        /** Get the **current** [Date].
         *
         * Calls [LocalDate.now] under the hood. */
        fun now(): Date {
            return Date(LocalDate.now())
        }

        /** Only allow [Date]s that occurred.
         * That is, dates that are in the *past or present*.
         *
         * @return a singleton that can be passed to [rememberDatePickerState][androidx.compose.material3.rememberDatePickerState],
         * which will disable any *future* dates in the [DatePicker][androidx.compose.material3.DatePicker],
         * only allowing *past or present* dates to be selected. */
        @OptIn(ExperimentalMaterial3Api::class)
        fun pastOrPresentDates(): SelectableDates {
            // Taken from https://stackoverflow.com/a/77678547/32115191
            return object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis <= System.currentTimeMillis()
                }
                override fun isSelectableYear(year: Int): Boolean {
                    return year <= LocalDate.now().year
                }
            }
        }
    }
}

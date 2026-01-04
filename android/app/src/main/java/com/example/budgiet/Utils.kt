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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.concurrent.Executors

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

/** An [Executor][java.util.concurrent.Executor] containing the *single thread* that will run *blocking tasks*.
 *
 * A normal [Thread] could be used here, but it's better to use an [Executor][java.util.concurrent.Executor]
 * for ease of pushing tasks to the thread. */
private val WORKER_THREAD = Executors.newSingleThreadExecutor()
/** The **ID** of the [Thread] in the *single-threaded executor* [WORKER_THREAD].
 *
 * After it is first initialized, the **ID** will not change,
 * because the code it runs will never *throw* an [Exception],
 * so the thread will not terminate until the end of the program.
 *
 * The value does not need to be put in a [Mutex][kotlinx.coroutines.sync.Mutex],
 * as only the worker thread can modify this value. */
private var WORKER_THREAD_ID: Long? = null

sealed class Result<out T> {
    class Ok<out T>(val value: T) : Result<T>()
    class Err(val error: Throwable) : Result<Nothing>()
}

/** Run a **task** in a *single-threaded* work Executor,
 * and [remember] the value in a [Composable].
 *
 * This function adds the **task** to the executor and immediately returns a `mutableStateOf(null)`.
 * While the task waits to be executed (and while it is being executed),
 * the *UI* thread can continue the rendering process without having to wait for work to be done.
 *
 * After the **task** is finished, the returned [MutableState] is updated to contain a value:
 * either the *success* value produced by the **task** Callback,
 * or an *error value* if the **task** threw an [Exception] ([Throwable]).
 *
 * > Note: If this function detects that it is being called from the **worker thread**,
 * > it will just run the *task* in the same thread without first pushing it to the Executor and waiting its turn.
 * > This optimizes the order of running *tasks* in case the caller calls [rememberWork] without knowing it is in the worker thread,
 * > Although this should be extremely rare. */
@Composable
fun <T> rememberWork(task: suspend () -> T): MutableState<Result<T>?> = remember {
    // Run on the current thread if it is the worker thread
    if (WORKER_THREAD_ID != null && Thread.currentThread().id == WORKER_THREAD_ID) {
        runBlocking {
            mutableStateOf(try {
                Result.Ok(task())
            } catch (e: Throwable) {
                Result.Err(e)
            })
        }
    } else {
        val state = mutableStateOf<Result<T>?>(null)

        WORKER_THREAD.execute {
            if (WORKER_THREAD_ID == null) {
                WORKER_THREAD_ID = Thread.currentThread().id
            }

            runBlocking {
                state.value = try {
                    Result.Ok(task())
                } catch (e: Throwable) {
                    Result.Err(e)
                }
            }
        }

        state
    }
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

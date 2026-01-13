package com.example.budgiet

import android.annotation.SuppressLint
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.concurrent.Executors

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
private fun isWorkerThread(): Boolean = WORKER_THREAD_ID != null && Thread.currentThread().id == WORKER_THREAD_ID

/** A *sum type* that represents an operation's **result**.
 *
 * A [Result] can be [Ok] if the operation was *successful* and provides a good value,
 * or [Err] if the operation *failed* and threw an [Exception] ([Throwable]).
 *
 * ## Example
 *
 * ```kotlin
 * when (val result = ...) {
 *     is Result.Ok -> useValue(result.value)
 *     is Result.Err -> useError(result.error)
 * }
 * ``` */
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
 * After the **task** is finished, the returned [MutableState] is updated to contain a [Result]:
 * either the *success* value produced by the **task** Callback,
 * or an *error value* if the **task** threw an [Exception] ([Throwable]).
 * Throwing an [Exception] in a [Composable] is not ideal since it will crash the program if not caught,
 * so this function will automatically catch [Exception]s and put it in the [Result] instead.
 *
 * > Note: If this function detects that it is being called from the **worker thread**,
 * > it will just run the *task* in the same thread without first pushing it to the Executor and waiting its turn.
 * > This optimizes the order of running *tasks* in case the caller calls [rememberWork] without knowing it is in the worker thread,
 * > Although this should be extremely rare. */
@Composable
fun <T> rememberWork(task: suspend () -> T): MutableState<Result<T>?> = remember {
    // Run on the current thread if it is the worker thread
    if (isWorkerThread()) {
        runBlocking {
            // Don't allow an exception to terminate the worker thread; gotta catch em all.
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
                // Don't allow an exception to terminate the worker thread; gotta catch em all.
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

/** Run a **task** in a *single-threaded* work Executor,
 * returning the value that the **task** produced.
 *
 * This function adds the **task** to the Executor and *suspends* while waiting for the **task** to produce a result.
 * Unlike [rememberWork], this function will *rethrow* any [Exception]s thrown by the **task**.
 * It is up to the caller to *catch* those [Exception]s.
 *
 * > Note: If this function detects that it is being called from the **worker thread**,
 * > it will just run the *task* in the same thread without first pushing it to the Executor and waiting its turn.
 * > This optimizes the order of running *tasks* in case the caller calls [runWork] without knowing it is in the worker thread,
 * > Although this should be extremely rare. */
suspend fun <T> runWork(task: suspend () -> T): Result<T> {
    return if (isWorkerThread()) {
        // Don't allow an exception to terminate the worker thread; gotta catch em all.
        try {
            Result.Ok(task())
        } catch (e: Throwable) {
            Result.Err(e)
        }
    } else {
        val channel = Channel<Result<T>>(capacity = 1)

        WORKER_THREAD.execute {
            if (WORKER_THREAD_ID == null) {
                WORKER_THREAD_ID = Thread.currentThread().id
            }

            // Don't know why it's complaining about this if the Runnable is not suspend, so it wouldn't compile anyways.
            @Suppress("RunBlockingInSuspendFunction")
            runBlocking {
                // Don't allow an exception to terminate the worker thread; gotta catch em all.
                channel.send(try {
                    Result.Ok(task())
                } catch (e: Throwable) {
                    Result.Err(e)
                })
            }
        }

        channel.receive()
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

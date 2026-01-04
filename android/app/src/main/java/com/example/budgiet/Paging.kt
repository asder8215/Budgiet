package com.example.budgiet

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState

// NOTE: key is NOT a Location ID, but an index in the pagination
typealias PagingKey = UInt

/** The **function** that is in charge of getting the **data** that will be loaded by the [ListPagingSource].
 * ```kotlin
 * suspend (query: CharSequence, startIndex: UInt, length: UInt) -> List<T>
 * ```
 *
 * This function *must be* ***pure*** and ***consecutive***.
 * That is, it must always return the same [List] if given the same arguments,
 * and must always return items in order,
 * such that calling it with consecutive **startIndices** yields *consecutive* items.
 *
 * Here is an example of how it should behave:
 * ```kotlin
 * getPage("", 0, 5) -> [ 1, 2, 3, 4, 5 ]
 * getPage("", 1, 5) -> [ 2, 3, 4, 5, 6 ]
 * ```
 *
 * ### Suspend
 *
 * A [PageGetter] runs in a **worker thread** and can adopt any *suspend* behavior they wish.
 * This allows the *UI* thread to keep rendering without stutters while the [PageGetter] is producing a result.
 *
 * ### Parameters
 *
 *  * **query** This is the string that the [androidx.compose.material3.SearchBar] linked to the [ListPagingSource]
 *     wants the getter to *query* for in a Database or an API endpoint.
 *     The getter can ignore the query if the getter does not have a structure to query.
 *
 *  * **startIndex** The index of the item that will be placed at the beginning of the *page*.
 *
 *  * **length** The amount of items that the *pager* is requesting.
 *     This value only serves as a guideline, because the returned [List] can have any *size*,
 *     but the size of the [List] tells the *pager* whether there are more pages to load or not.
 *
 *     More info about this in the return.
 *
 * @return The *data* that will populate the **page**.
 *
 *   The **size** of the [List] should not exceed the **length** that the Pager requested.
 *   If it does exceed it, the Pager will [List] take a slice with only the required elements.
 *
 *   If the **size** of the [List] is less than the requested **length**,
 *   the *pager* will assume that there are no more items in the dataset and will not request any further pages. */
typealias PageGetter<T> = suspend (CharSequence, UInt, UInt) -> List<T>

/** Create a [Pager] that persists in a [Composable].
 *
 * See [ListPagingSource] for parameters.
 *
 * ### Example
 *
 * ```kotlin
 * val searchPager = rememberListPager(...)
 * val pagedItems = searchPager.flow.collectAsLazyPagingItems()
 * ```  */
@Composable
fun <T: Any> rememberListPager(
    /**```kotlin
     * suspend (query: CharSequence, startIndex: UInt, length: UInt) -> List<T>
     * ```
     * See [PageGetter]. */
    getPage: PageGetter<T>,
    searchState: TextFieldState? = null,
    config: PagingConfig,
): Pager<PagingKey, T> = remember {
    Pager(config) { ListPagingSource(getPage, searchState) }
}

/** A generic [PagingSource] over a list of items.
 *
 * This class was mainly designed to be used for *text searches* that query a *database*,
 * but the implementation allows it to be *general purpose*,
 * as long as **getPage** conforms to [PageGetter]'s expected behavior.
 *
 * ### Parameters
 *
 *  * **getPage**: Callback that gets the data for a Page. See [PageGetter].
 *
 *  * **searchState**: The *state* value of the [androidx.compose.material3.SearchBar],
 *     which holds the **query** text.
 *
 *     Ideally, only a reference to the **query** text should be passed in here,
 *     but I don't think this is possible, so the whole *state* must be passed in. */
class ListPagingSource<T: Any>(
    /**```kotlin
     * suspend (query: CharSequence, startIndex: UInt, length: UInt) -> List<T>
     * ```
     * See [PageGetter]. */
    val getPage: PageGetter<T>,
    val searchState: TextFieldState? = null,
) : PagingSource<PagingKey, T>() {
    /** Return `null` if the **key** is out of bounds.
     *
     * Made the argument type [Int] instead of [UInt] to avoid underflow,
     * but **key** should always be [UInt] everywhere else. */
    private fun ensureValidKey(key: Int): PagingKey? = when {
        key >= 0 -> key.toUInt()
        else -> null
    }

    override suspend fun load(params: LoadParams<PagingKey>): LoadResult<PagingKey, T> {
        // Don't perform a page query if the query is empty or null
        if (this.searchState == null || this.searchState.text.isEmpty()
        || params.loadSize < 0)
            return LoadResult.Page(
                data = listOf(),
                prevKey = null,
                nextKey = null,
            )

        // If params.key is null, it is the first load, so we start loading with STARTING_KEY
        val start = params.key ?: 0u
        // Call getPage from a worker thread
        val data = runWork {
            getPage(searchState.text, start, params.loadSize.toUInt())
                // Ignore items that don't fit on this page. They should be loaded on the next page
                .take(params.loadSize)
        }

        return LoadResult.Page(
            data = data,
            // This will be the startKey of the next load() if it's going backwards
            prevKey = ensureValidKey(start.toInt() - data.size),
            // Only provide a nextKey if there are potentially more items to load
            nextKey = if (data.size >= params.loadSize) {
                start + data.size.toUInt()
            } else {
                null
            },
        )
    }

    override fun getRefreshKey(state: PagingState<PagingKey, T>): PagingKey? {
        val anchorPosition = state.anchorPosition ?: return null
        val key = (anchorPosition - state.config.initialLoadSize / 2)
            .coerceAtLeast(0)
        return ensureValidKey(key)
    }
}

class ListPagingTests {
    // TODO
    // TODO: test with data.size < pageSize, == pageSize, > pageSize
    // TODO: test that load does not hold the UI thread
    // TODO: test that loading indicator item shows up when loading takes long (do 5 secs).
    //       The indicator should show up when the search is initiated, when scrolling down, and scrolling up
}

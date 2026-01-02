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
typealias PageGetter<T> = (CharSequence, PagingKey, UInt) -> List<T>

// TODO: doc
@Composable
fun <T: Any> rememberListPager(
    getPage: PageGetter<T>,
    searchState: TextFieldState? = null,
    config: PagingConfig,
): Pager<PagingKey, T> = remember {
    Pager(config) { ListPagingSource(getPage, searchState) }
}

/** A generic [PagingSource] over a list of items.
 * TODO: doc */
class ListPagingSource<T: Any>(
    // TODO: doc: MUST return a list sized lesser or equal to the passed in size
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
        if (this.searchState == null /* || this.searchState.text.isEmpty()*/
        || params.loadSize < 0)
            return LoadResult.Page(
                data = listOf(),
                prevKey = null,
                nextKey = null,
            )

        // TODO: test that loading indicator shows up with sleep(1000)

        // If params.key is null, it is the first load, so we start loading with STARTING_KEY
        val start = params.key ?: 0u
        // Returns a list of bogus locations for now
        val data = getPage(searchState.text, start, params.loadSize.toUInt())
            // Ignore items that don't fit on this page. They should be loaded on the next page
            .take(params.loadSize)

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
}

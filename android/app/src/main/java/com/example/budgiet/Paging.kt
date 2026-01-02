package com.example.budgiet

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
    query: CharSequence?,
    config: PagingConfig,
): Pager<PagingKey, T> = remember {
    Pager(config) { ListPagingSource(getPage, query) }
}

/** A generic [PagingSource] over a list of items.
 * TODO: doc */
class ListPagingSource<T: Any>(
    val getPage: PageGetter<T>,
    // TODO: doc: must be a mutable, as value can't be changed later
    val query: CharSequence? = null,
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
        println("load(query = $query)")

        // Don't perform a page query if the query is empty or null
        if (this.query?.isEmpty() ?: false)
            return LoadResult.Page(
                data = listOf(),
                prevKey = null,
                nextKey = null,
            )


        // TODO: test that loading indicator shows up with sleep(1000)

        // If params.key is null, it is the first load, so we start loading with STARTING_KEY
        val start = params.key ?: 0u
        // Returns a list of bogus locations for now
        val data = getPage(query!!, start, params.loadSize.toUInt())

        return LoadResult.Page(
            data = data,
            // This will be the startKey of the next load() if it's going backwards
            prevKey = ensureValidKey(start.toInt() - data.size),
            // Next start key is the next index in the search list
            nextKey = start + data.size.toUInt(),
        )
    }

    override fun getRefreshKey(state: PagingState<PagingKey, T>): PagingKey? {
        val anchorPosition = state.anchorPosition ?: return null
        // Should never return null here
        return (anchorPosition - state.config.initialLoadSize / 2)
            .coerceAtLeast(0)
            .toUInt()
    }
}

class ListPagingTests {
    // TODO
}

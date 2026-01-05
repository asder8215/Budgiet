package com.example.budgiet

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.testing.asSnapshot
import junit.framework.AssertionFailedError
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.math.min

class PagingUnitTests {
    /** How many **items** a page has.
     * In this case, this is analogous to how many items are visible,
     * however, in the UI only a *portion* of a page is visible to the user. */
    val pageSize = 4u
    /** How many **pages** can be loaded at a time. */
    val maxPages = 3u

    fun <T: Any> newPager(source: ListPagingSource<T>) = Pager(config = PagingConfig(
        // These config values are only for testing, and are very different in production.
        pageSize = pageSize.toInt(),
        initialLoadSize = pageSize.toInt(),
        // Only load pages that are actually requested (doesn't seem to work).
        prefetchDistance = pageSize.toInt() - 1,
        maxSize = (maxPages * pageSize).toInt(),
    )) { source }

    /** Generate a [List] that contains *at most* the number of items that can fit in **numPages**.
     *
     * The greatest (last) item can only be as high as the number of items that fit in **numPages**.
     * aka `list.last() < numPages * pageSize`.
     *
     * @param numPages How many **pages** of items can be generated.
     * @param start is the *first* item in the [List].
     *        It is also a way to tell what page it is on.
     * @param length is how many items the list contains. */
    fun genList(numPages: UInt?, start: UInt, length: UInt): List<Int> {
        if (numPages == null)
            return List(length.toInt()) { i -> start.toInt() + i }

        val maxItem = numPages * pageSize
        // start index is out of bounds
        if (start > maxItem)
            return listOf()

        // List should only contain items within the number of pages that it can generate
        return List(min(length, maxItem - start).toInt())
            { i -> start.toInt() + i }
    }

    /** Same as [androidx.paging.testing.SnapshotLoader.scrollTo].
     *
     * @return all loaded **pages** in the [Pager]. */
    fun <K: Any, V: Any> Pager<K, V>.scrollTo(index: UInt): List<V> = runBlocking {
        this@scrollTo.flow.asSnapshot {
            this.scrollTo(index.toInt())
        }
    }

    @Test
    fun pageByPageTest() {
        // Keep a list of what pages get created in load()
        val generatedPages = mutableListOf<List<Int>>()
        // Pager loads infinite pages
        val infinitePager = newPager(ListPagingSource.withoutQuery { start, length ->
            val page = genList(null, start, length)
            generatedPages.add(page)
            page
        })

        // It seems that the Pager will always load at least 2 pages in the initial load, even if you tell it to only load 1
        // // Scroll to first item, generate first page.
        // assertEquals(
        //     actual = infinitePager.scrollTo(0u),
        //     expected = (0..<pageSize.toInt()).toList(),
        // )

        // Scroll to the second page.
        assertEquals(
            actual = infinitePager.scrollTo(pageSize),
            // Only 2 pages should be loaded.
            expected = (0..<pageSize.toInt() * 2).toList(),
        )
        // Check that the pages are fragmented correctly.
        assertEquals(
            actual = generatedPages,
            expected = listOf(listOf(0, 1, 2, 3), listOf(4, 5, 6, 7)),
        )

        // FIXME!!!: All previous pages (start = 0) are regenerated when loading a next page, even if the page is outside the maxPages range

        // Scroll to the third page (max loaded).
        assertEquals(
            actual = infinitePager.scrollTo((maxPages - 1u) * pageSize),
            // Only 3 pages should be loaded.
            expected = (0..<(maxPages * pageSize).toInt()).toList(),
        )
        // Check that the pages are fragmented correctly.
        assertEquals(
            actual = generatedPages,
            expected = listOf(listOf(0, 1, 2, 3), listOf(4, 5, 6, 7), listOf(8, 9, 10, 11)),
        )

        // Scroll to the fourth page (unload 1)
        assertEquals(
            actual = infinitePager.scrollTo(maxPages * pageSize),
            // Only 3 pages should be loaded.
            expected = (0..<((maxPages + 1u) * pageSize).toInt()).toList(),
        )
        // Check that the pages are fragmented correctly.
        assertEquals(
            // After loading maxPages, the first page will be unloaded when loading the next page.
            actual = run {
                generatedPages.removeFirst()
                generatedPages
            },
            expected = listOf(listOf(4, 5, 6, 7), listOf(8, 9, 10, 11), listOf(12, 13, 14, 15)),
        )
    }

    // TODO: test with data.size < pageSize, == pageSize, > pageSize
    // TODO: test that load does not hold the UI thread
    // TODO: test that loading indicator item shows up when loading takes long (do 5 secs).
    //       The indicator should show up when the search is initiated, when scrolling down, and scrolling up
    // TODO: test that previous pages are unloaded when scrolling far enough, and that the same items are loaded back in when scrolling back

    /** Testing that the logic for genList() works */
    @Test
    fun genListTest() {
        val numPages = maxPages
        // Requested list with exactly enough items to fit in the page
        var list = genList(numPages, 0u, numPages * pageSize)
        assertEquals(
            actual = list,
            expected = (0..<(numPages * pageSize).toInt()).toList(),
        )
        list = genList(numPages, 4u, 4u)
        assertEquals(
            actual = list,
            expected = (4..7).toList(),
        )
        // Requested a list where all items are outside the pages bound
        list = genList(numPages, numPages * pageSize, 9u)
        assertEquals(
            actual = list,
            expected = listOf(),
        )
        // Requested a list where some items are outside the pages bound
        list = genList(numPages, 6u, 9999u)
        assertEquals(
            actual = list,
            expected = (6..<(numPages * pageSize).toInt()).toList()
        )
        list = genList(0u, 6u, 9u)
        assertEquals(
            actual = list,
            expected = listOf()
        )
    }
}

// WHY ISN'T THERE AN IMPLEMENTATION OF THIS FOR LIST
fun <T> assertEquals(actual: List<T>, expected: List<T>) {
    fun <T> isEqual(one: List<T>, two: List<T>): Boolean {
        if (one.size != two.size)
            return false

        for (i in 0..<one.size) {
            if (one[i] != two[i])
                return false
        }

        return true
    }

    if (!isEqual(actual, expected))
        // FIXME: why message prints twice
        throw AssertionFailedError("Assertion failed:\n\texpected = $expected\n\tactual = $actual")
}

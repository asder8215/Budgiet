package com.example.budgiet

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import junit.framework.AssertionFailedError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.math.min
import kotlin.time.measureTime

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
     * @param start The *first* item in the [List].
     *        It is also a way to tell what page it is on.
     * @param length How many items are requested for this specific page. */
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
    fun <T: Any> Flow<PagingData<T>>.scrollTo(index: UInt): List<T> = runBlocking {
        this@scrollTo.asSnapshot {
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
        val infiniteFlow = infinitePager.flow

        // It seems that the Pager will always load at least 2 pages in the initial load, even if you tell it to only load 1
        // // Scroll to first item, generate first page.
        // assertEquals(
        //     actual = infinitePager.scrollTo(0u),
        //     expected = (0..<pageSize.toInt()).toList(),
        // )

        // Scroll to the second page.
        assertEquals(
            actual = infiniteFlow.scrollTo(pageSize),
            // Only 2 pages should be loaded.
            expected = (0..<(pageSize * 2u).toInt()).toList(),
        )
        // Check that the pages are fragmented correctly.
        assertEquals(
            actual = generatedPages,
            expected = listOf(listOf(0, 1, 2, 3), listOf(4, 5, 6, 7)),
        )

        // FIXME!!!: All previous pages (start = 0) are regenerated when loading a next page, even if the page is outside the maxPages range

        // Scroll to the third page (max loaded).
        assertEquals(
            actual = infiniteFlow.scrollTo((maxPages - 1u) * pageSize),
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
            actual = infiniteFlow.scrollTo(maxPages * pageSize),
            // Only 3 pages should be loaded.
            expected = (pageSize.toInt()..<((maxPages + 1u) * pageSize).toInt()).toList(),
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

        // Scroll back to the first page (unloaded page should be reloaded, and last page should be unloaded).
        generatedPages.removeLast()
        assertEquals(
            actual = infiniteFlow.scrollTo(0u),
            // Only 3 pages should be loaded.
            expected = (0..<(maxPages * pageSize).toInt()).toList(),
        )
        // Check that the pages are fragmented correctly.
        assertEquals(
            actual = run {
                generatedPages.removeFirst()
                generatedPages
            },
            // First page is appended to the end of the loaded pages.
            expected = listOf(listOf(4, 5, 6, 7), listOf(8, 9, 10, 11), listOf(0, 1, 2, 3)),
        )
    }

    /** Test that the Pager stops trying to load items when the data size is smaller than the page size. */
    @Test
    fun smallerDataSize() {
        /** How many pages should be full pages (i.e. contain the amount of items requested).
         *
         * Use maxPages to avoid unload of first page.
         * That is tested in pageByPageTest(). */
        val numFullPages = maxPages - 1u
        val partialPageSize = pageSize - 2u
        val smallerPager = newPager(ListPagingSource.withoutQuery { start, length ->
            if (start >= numFullPages * pageSize) {
                // For the last page, return less data than requested.
                genList(numFullPages + 1u, start, partialPageSize)
            } else {
                genList(numFullPages, start, length)
            }
        })
        val smallerFlow = smallerPager.flow

        // Scroll to the last full page (should all be full pages).
        assertEquals(
            actual = smallerFlow.scrollTo((numFullPages - 1u) * pageSize),
            expected = (0..<(numFullPages * pageSize).toInt()).toList()
        )

        // Scroll to the last page (should be partially filled).
        assertEquals(
            actual = smallerFlow.scrollTo(numFullPages * pageSize),
            expected = (0..<((numFullPages * pageSize) + partialPageSize).toInt()).toList()
        )

        // Scroll past last page (should be empty).
        assertEquals(
            actual = smallerFlow.scrollTo((numFullPages + 1u) * pageSize),
            expected = (0..<((numFullPages * pageSize) + partialPageSize).toInt()).toList()
        )
    }

    /** Test that the Pager keeps trying to load items when the data size is the same as the page size.
     * The pager should also stop trying to load items when the data comes up empty. */
    @Test
    fun exactDataSize() {
        /** How many pages should be generated.
         *
         * Use maxPages to avoid unload of first page.
         * That is tested in pageByPageTest(). */
        val numPages = maxPages - 1u
        val exactPager = newPager(ListPagingSource.withoutQuery { start, length ->
            genList(numPages, start, length)
        })
        val exactFlow = exactPager.flow

        // Scroll to the last page.
        assertEquals(
            actual = exactFlow.scrollTo((numPages - 1u) * pageSize),
            expected = (0..<(numPages * pageSize).toInt()).toList()
        )

        // Scroll past last page (should be empty).
        assertEquals(
            actual = exactFlow.scrollTo(numPages * pageSize),
            expected = (0..<(numPages * pageSize).toInt()).toList()
        )
    }

    /** Test that the Pager ignores last items when the data size is larger than the page size. */
    @Test
    fun largerDataSize() {
        /** How many pages should be have the exact amount of items requested.
         *
         * Use maxPages to avoid unload of first page.
         * That is tested in pageByPageTest(). */
        val numExactPages = maxPages - 1u
        /** How many extra items should be generated for the last page. */
        val extraPageSize = 3u // Arbitrary value
        val largerPager = newPager(ListPagingSource.withoutQuery { start, length ->
            if (start == numExactPages * pageSize) {
                // For the last page, return less data than requested.
                List((pageSize + extraPageSize).toInt()) { i -> start.toInt() + i }
            } else {
                genList(numExactPages, start, length)
            }
        })
        val largerFlow = largerPager.flow

        // Scroll to the last full page (should all be full pages).
        assertEquals(
            actual = largerFlow.scrollTo((numExactPages - 1u) * pageSize),
            expected = (0..<(numExactPages * pageSize).toInt()).toList()
        )

        // Scroll to the last page (should ignore extra items).
        assertEquals(
            actual = largerFlow.scrollTo(numExactPages * pageSize),
            expected = (0..<((numExactPages + 1u) * pageSize).toInt()).toList()
        )

        // Scroll past last page (should be empty).
        assertEquals(
            actual = largerFlow.scrollTo((numExactPages + 1u) * pageSize),
            expected = (0..<((numExactPages * pageSize + 1u) + extraPageSize).toInt()).toList()
        )
    }

    // This seems fundamentally impossible to test because the suspend primitives callable from a non-suspend block will block until the coroutine is finished,
    // or will  create a Job in another thread, which defeats the purpose of checking if the PagingSource executes the loader in a thread other than the current thread.
    // We just have to trust that the UI thread will not be blocked while the worker thread executes the loader.
    //
    // /* Test that load does not block the UI thread. */
    // @Test
    // fun blockThreadTest() {
    //     val sleepingTime = 5000L
    //     val sleepingPager = newPager(ListPagingSource.withoutQuery { start, length ->
    //         Thread.sleep(sleepingTime)
    //         genList(1u, start, length)
    //     })
    //
    //     val duration = measureTime {
    //         CoroutineScope(Dispatchers.IO).launch {
    //             sleepingPager.flow.launchIn(this)
    //         }
    //     }
    //     assert(duration.inWholeMilliseconds < sleepingTime)
    // }

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

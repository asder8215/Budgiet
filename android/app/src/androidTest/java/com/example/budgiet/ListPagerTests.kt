package com.example.budgiet

import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertAll
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onChild
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.printToString
import androidx.paging.PagingConfig
import com.example.budgiet.ui.utils.PagedListColumn
import com.example.budgiet.ui.utils.PagerController
import kotlinx.coroutines.delay
import org.junit.Rule
import org.junit.Test

const val LIST_TAG = "PagedList"
const val ITEM_TAG = "DataItem"
const val LOADING_ITEM_TAG = "LoadingItem"
const val ERROR_ITEM_TAG = "ErrorItem"
const val PAGE_SIZE = 4
const val MAX_PAGES = 3
const val ERROR_MESSAGE = "loading page exception"

class TestState(
    private val rule: ComposeContentTestRule,
    private val getPage: PageGetter<Int> = { start, length -> List(length.toInt()) { i -> start.toInt() + i } },
) {
    val pagerController = PagerController()

    val listColumn
        get() = rule.onNodeWithTag(LIST_TAG)

    init {
        rule.setContent {
            PagedListColumn(
                modifier = Modifier.testTag(LIST_TAG),
                pager = rememberListPager(
                    getPage = getPage,
                    config = PagingConfig(
                        pageSize = PAGE_SIZE,
                        initialLoadSize = PAGE_SIZE,
                        prefetchDistance = PAGE_SIZE,
                        maxSize = PAGE_SIZE * MAX_PAGES,
                        // Don't let the pager return a bunch of unloaded items, we are going to show a single unloaded item at a time.
                        enablePlaceholders = false,
                    )
                ),
                pagerController = this.pagerController,
                itemKey = { it },
                itemContent = { i -> this.DataItem(
                    modifier = Modifier.testTag(ITEM_TAG),
                    headlineContent = { Text("Item: $i") },
                ) },
                loadingContent = { this.LoadingItem(modifier = Modifier.testTag(LOADING_ITEM_TAG)) },
                errorContent = { type, message ->
                    this.ErrorItem(modifier = Modifier.testTag(ERROR_ITEM_TAG), type, message)
                }
            )
        }
    }
}

class ListPagerTests {
    @get:Rule
    val rule = createComposeRule()

    // TODO: test that loading indicator item shows up when loading takes long (do 5 secs).
    //       The indicator should show up when the search is initiated, when scrolling down, and scrolling up

    // TODO: test that items are unloaded

    // TODO: test color of error item

    @Test
    fun unloadPages() {
        val state = TestState(this.rule)

        // Scroll to page after max page.
        for (i in PAGE_SIZE..(PAGE_SIZE * MAX_PAGES + 1)) {
            state.listColumn.performScrollToIndex(i)
            println("Scrolled to $i")
        }

        println("children = ${state.listColumn.onChildren().printToString()}")

        // TODO: check that all items loaded are only in the range of page 2 to MAX_PAGES
        TODO()
    }

    @Test
    fun refreshLoading() {
        val state = TestState(this.rule) { _, _ -> delay(5000); listOf(0, 1, 2, 3) }

        println("children = ${state.listColumn.onChildren().printToString()}")

        state.listColumn.onChild()
            .assert(hasTestTag(LOADING_ITEM_TAG))
    }

    @Test
    fun refreshError() {
        val state = TestState(this.rule) { _, _ -> throw Exception(ERROR_MESSAGE) }

        state.listColumn.onChild()
            .assertErrorItem()
    }

    @Test
    fun prependLoading() {
        TODO()
    }

    @Test
    fun prependError() {
        var firstPageUnloaded = false
        val state = TestState(this.rule) { start, length ->
            val start = start.toInt()
            val length = length.toInt()
            println("load page ${1 + PAGE_SIZE / start} { start = $start, length = $length }")
            // The first page gets unloaded when the page that passes the MAX_PAGES count gets loaded.
            if (start >= PAGE_SIZE * MAX_PAGES) {
                firstPageUnloaded = true
            }

            when {
                start < PAGE_SIZE && firstPageUnloaded -> throw Exception(ERROR_MESSAGE)
                else -> List(length) { i -> start + i }
            }
        }

        // Scroll to page after max page.
        for (i in PAGE_SIZE..(PAGE_SIZE * MAX_PAGES + 1)) {
            state.listColumn.performScrollToIndex(i)
            println("Scrolled to $i")
        }

        println("children = ${state.listColumn.onChildren().printToString()}")

        // Check that all initially loaded items are good data.
        state.listColumn.onChildren()
            .assertAll(hasTestTag(ITEM_TAG))

        // Scroll back to first loaded page.
        state.listColumn.performScrollToIndex(PAGE_SIZE)

        state.listColumn.onChildren()
            .onFirst()
            .assertErrorItem()
    }

    @Test
    fun appendLoading() {
        TODO()
    }

    @Test
    fun appendError() {
        val state = TestState(this.rule) { start, length ->
            val start = start.toInt()
            val length = length.toInt()
            when {
                // Must throw error on 3rd page because Pager will ALWAYS initial load 2 pages,
                // without regarding the initialLoadSize the config.
                start < PAGE_SIZE * 2 -> List(length) { i -> start + i }
                else -> throw Exception(ERROR_MESSAGE)
            }
        }

        // Check that all initially loaded items are good data.
        state.listColumn.onChildren()
            .assertAll(hasTestTag(ITEM_TAG))

        // Scroll to load error item
        state.listColumn.performScrollToIndex(PAGE_SIZE)
        state.listColumn.performScrollToIndex(PAGE_SIZE * 2)
        state.listColumn.onChildren()
            .onLast()
            .assertErrorItem()
    }
}

fun SemanticsNodeInteraction.assertErrorItem(): SemanticsNodeInteraction
    = this.assertContentDescriptionEquals("Error")
        .assert(hasTestTag(ERROR_ITEM_TAG))
        .assertTextEquals("Error: ${Exception::class.java.name}", ERROR_MESSAGE)

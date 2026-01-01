package com.example.budgiet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.paging.Pager
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.budgiet.ui.theme.BudgietTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BudgietTheme {
                MainPage(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPage(modifier: Modifier = Modifier) {
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            PlainToolTipBox(text = "Add new transaction record") {
                FloatingActionButton(onClick = { showBottomSheet = true }) {
                    Icon(Icons.Filled.Add, "New Transaction")
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Press the '+ Transaction' button to get started.")
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            modifier = Modifier.fillMaxHeight()
                .windowInsetsPadding(WindowInsets.statusBars),
            sheetState = sheetState,
            onDismissRequest = { showBottomSheet = false },
        ) {
            NewTransactionForm()
        }
    }
}

class Location(
    val id: UInt,
    val name: String,
    val address: String,
)
fun getRecentLocations(start: UInt = 0u, length: UInt = 10u): List<Location> {
    // Returns a list of bogus locations for now
    return List(length.toInt()) { i ->
        val id = i.toUInt() + start
        if (id % 2u == 0u) {
            Location(id = id, name = "Chipotle", "$id$id$id Main Street, Bronx NY")
        } else {
            Location(id = id, name = "Aldi", "$id$id$id IsNuts Lane, Los Angeles CA")
        }
    }
}
// NOTE: key is NOT a Location ID, but an index in the pagination
class ListSource(
    val query: CharSequence,
) : PagingSource<UInt, Location>() {
    override suspend fun load(params: LoadParams<UInt>): LoadResult<UInt, Location> {
        // If params.key is null, it is the first load, so we start loading with STARTING_KEY
        val start = params.key ?: 0u
        // Returns a list of bogus locations for now
        val data = getRecentLocations(start, params.loadSize.toUInt())
            .filter { location -> location.name.startsWith(query) }

        // TODO: how to pass search query to LoadParams?
        return TODO()
    }

    override fun getRefreshKey(state: PagingState<UInt, Location>): UInt? {
        TODO("Not yet implemented")
    }
}
fun searchLocations(query: CharSequence): PagingSource<UInt, Location> {
    return ListSource(query)
}

@Preview(showBackground = true)
@Composable
fun MainPagePreview() {
    BudgietTheme {
        MainPage()
    }
}

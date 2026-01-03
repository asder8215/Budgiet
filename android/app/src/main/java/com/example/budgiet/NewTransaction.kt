package com.example.budgiet

import android.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.example.budgiet.ui.theme.BudgietTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewTransactionForm(modifier: Modifier = Modifier) {
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(Date.now()) }
    var showLocationPicker by remember { mutableStateOf(false) }
    var selectedLocation by remember { mutableStateOf<Location?>(null) }
    var selectedPrice by remember { mutableStateOf("") }

    Column(
        modifier = modifier,
    ) {
        FormField("Date") {
            OutlinedTextField(
                readOnly = true,
                onValueChange = {},
                value = selectedDate.toString(),
                trailingIcon = {
                    PlainToolTipBox("Select Date") {
                        IconButton(onClick = { showDatePicker = !showDatePicker }) {
                            Icon(Icons.Filled.DateRange, "Select Date")
                        }
                    }
                },
            )
        }
        FormField("Location") {
            OutlinedButton(onClick = { showLocationPicker = true }) {
                Text(
                    if (selectedLocation != null) {
                        selectedLocation!!.name
                    } else {
                        "Select Location"
                    }
                )
            }
            PlainToolTipBox("Auto-select Location") {
                FilledIconButton(onClick = { TODO() }) {
                    Icon(Icons.Outlined.LocationOn, "Auto-select Location")
                }
            }
        }
        FormField("Price") {
            var parseError by remember { mutableStateOf(false) }
            OutlinedTextField(
                onValueChange = {
                    /* TODO: current behavior is that only decimal
                     * values are accepted; we should be able to
                     * accept pasted values and parse through them,
                     * producing an error message on why the value is invalid
                     */
                    try {
                        if (it != "") {
                            it.toBigDecimal()
                        }
                        parseError = false
                    } catch (_: NumberFormatException) {
                        parseError = true
                    }
                    selectedPrice = it
                },
                value = selectedPrice,
                modifier = Modifier
                    // fixme: use clamping than max for width
                    .widthIn(max = 150.dp)
                    .testTag("price_input_field"),
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number
                ),
                isError = parseError,
                placeholder = {
                    Text(
                        "0",
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .fillMaxWidth(),
                        color = androidx.compose.ui.graphics.Color(Color.GRAY)
                    )
                },
                supportingText = {
                    if (parseError) {
                        val errorMsg = "$selectedPrice is not a valid price value"
                        Text(errorMsg)
                    }
                },
                leadingIcon = {
                    Icon(Icons.Filled.AttachMoney, "Currency")
                },
            )
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            selectableDates = Date.pastOrPresentDates(),
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                        // FIXME: DatePicker is providing incorrect dates
                        datePickerState.selectedDateMillis?.let {
                            selectedDate = Date(it)
                        }
                    }
                ) {
                    Text("Ok")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePicker = false }
                ) {
                    Text("Cancel")
                }
            },
        ) {
            DatePicker(
                state = datePickerState,
            )
        }
    }

    if (showLocationPicker) {
        LocationPickerDialog(
            onDismiss = { showLocationPicker = false },
            onSubmit = { location -> selectedLocation = location }
        )
    }

}

@Composable
fun FormField(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable (RowScope.() -> Unit)
) {
    ListItem(
        modifier = modifier,
        leadingContent = { Text(label) },
        headlineContent = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                content = content,
            )
        }
    )
//    Row(
//        modifier = modifier
//            .fillMaxWidth()
//            .padding(horizontal = 6.dp, vertical = 2.dp),
//        horizontalArrangement = Arrangement.SpaceBetween,
//        verticalAlignment = Alignment.CenterVertically,
//    ) {
//        Text(label)
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.End,
//            verticalAlignment = Alignment.CenterVertically,
//            content = content,
//        )
//    }
}

@Composable
fun LocationPickerDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onSubmit: (Location) -> Unit,
) {
    val dialogPadding = 8.dp
    val textIconButtonPadding = 12.dp
    val textIconButtonSpacing = 4.dp
    val searchPageSize = 10u
    val searchState = rememberTextFieldState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = true,
        )
    ) {
        Card(
            modifier = modifier.fillMaxWidth() // PRO TIP: doesn't actually fill max width, it has a margin
        ) {
            Column(
                modifier = Modifier.padding(all = dialogPadding)
                // TODO: Animate height
            ) {
                PlainSearchBar(state = searchState)

                // Show search results if the SearchBar has a query,
                // otherwise show recent locations.
                if (searchState.text.isEmpty()) {
                    Text(
                        "Recent",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = dialogPadding)
                    )
                } else {
                    Spacer(Modifier.height(dialogPadding))
                }

                val dividerThickness = DividerDefaults.Thickness
                val localDensity = LocalDensity.current
                // Get the height of the first item in the list to determine the size of the whole List widget.
                // Give it a default in case the item's height could not be obtained.
                var itemHeight by remember { mutableStateOf(70.5.dp) }

                @Composable
                fun LocationItem(location: Location, modifier: Modifier = Modifier) {
                    ListItem(
                        modifier = modifier
                            .onGloballyPositioned { coords ->
                                itemHeight = with(localDensity) { coords.size.height.toDp() }
                            }
                            .clip(RoundedCornerShape(4.dp))
                            .clickable(onClick = { onSubmit(location) }),
                        headlineContent = { Text(location.name) },
                        supportingContent = { Text(location.address) },
                    )
                }

                @Composable
                fun LoadingItem(modifier: Modifier = Modifier) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        ListItem(
                            modifier = modifier
                                .clip(RoundedCornerShape(4.dp)),
                            headlineContent = { }
                        )
                        CircularProgressIndicator()
                    }
                }

                val searchPager = remember {
                    Pager(
                        config = PagingConfig(pageSize = searchPageSize.toInt())
                    ) { searchLocations(searchState.text) }
                }
                val pagedItems = searchPager.flow.collectAsLazyPagingItems()

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(dividerThickness),
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        // List's height is enough to show only 3.5 items (+ their dividers).
                        // TODO: use clamp
                        .heightIn(max = itemHeight * 3.5f + dividerThickness * 3),
                ) {
                    // Show search results if the SearchBar has a query,
                    // otherwise show recent locations
                    if (searchState.text.isEmpty()) {
                        items(
                            getRecentLocations(),
                            key = { location -> location.id }
                        ) { location ->
                            LocationItem(location)
                        }
                    } else {
                        items(
                            pagedItems.itemCount,
                            key = pagedItems.itemKey { location -> location.id }
                        ) { index ->
                            pagedItems[index]?.let { location ->
                                LocationItem(location)
                            } ?: run {
                                LoadingItem()
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = dialogPadding / 2),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    PlainToolTipBox("Add new location") {
                        Button(
                            onClick = { TODO() },
                            contentPadding = PaddingValues(horizontal = textIconButtonPadding)
                        ) {
                            Icon(Icons.Filled.Add, "New Location")
                            Spacer(Modifier.width(textIconButtonSpacing))
                            Text("New")
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NewTransactionPreview() {
    BudgietTheme {
        NewTransactionForm()
    }
}

@Preview(showBackground = true)
@Composable
fun LocationPickerPreview() {
    BudgietTheme {
        LocationPickerDialog(
            onDismiss = {},
            onSubmit = {},
        )
    }
}

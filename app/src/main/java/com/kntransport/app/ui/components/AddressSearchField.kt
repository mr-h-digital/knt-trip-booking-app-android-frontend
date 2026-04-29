package com.kntransport.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.kntransport.app.ui.theme.LocalAppColors
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Address text field with a Google Places autocomplete dropdown.
 * Debounces input by 300 ms before firing the Places API to avoid hammering
 * the quota. Selecting a suggestion resolves the full formatted address and
 * calls [onAddressSelected] with the human-readable string.
 */
@OptIn(FlowPreview::class)
@Composable
fun AddressSearchField(
    value          : String,
    onValueChange  : (String) -> Unit,
    onAddressSelected: (String) -> Unit,
    label          : String,
    placesClient   : PlacesClient,
    modifier       : Modifier = Modifier,
    leadingIcon    : ImageVector = Icons.Rounded.LocationOn,
) {
    val c       = LocalAppColors.current
    val scope   = rememberCoroutineScope()
    val context = LocalContext.current

    var predictions by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    var isLoading   by remember { mutableStateOf(false) }

    // Debounced query flow — avoids a Places API call on every keystroke
    val queryFlow = remember { MutableStateFlow("") }
    LaunchedEffect(Unit) {
        queryFlow
            .debounce(300L)
            .distinctUntilChanged()
            .filter { it.length >= 3 }
            .collect { query ->
                isLoading = true
                try {
                    val request = FindAutocompletePredictionsRequest.builder()
                        .setQuery(query)
                        // Bias results to South Africa
                        .setCountries(listOf("ZA"))
                        .build()
                    val response = placesClient.findAutocompletePredictions(request).await()
                    predictions = response.autocompletePredictions
                } catch (_: Exception) {
                    predictions = emptyList()
                } finally {
                    isLoading = false
                }
            }
    }

    Column(modifier = modifier) {
        KntTextField(
            value         = value,
            onValueChange = { text ->
                onValueChange(text)
                // Clear suggestions immediately when user clears the field
                if (text.length < 3) predictions = emptyList()
                scope.launch { queryFlow.emit(text) }
            },
            label       = label,
            leadingIcon = leadingIcon,
        )

        // Dropdown suggestions
        if (predictions.isNotEmpty() || isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                    .background(c.surface1)
                    .border(1.dp, c.borderColor,
                        RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)),
            ) {
                if (isLoading) {
                    Box(
                        Modifier.fillMaxWidth().padding(12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color       = c.blue,
                        )
                    }
                } else {
                    predictions.forEachIndexed { index, prediction ->
                        if (index > 0) {
                            HorizontalDivider(color = c.borderColor)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    // Fetch the full place details to get a clean formatted address
                                    scope.launch {
                                        try {
                                            val fetchRequest = FetchPlaceRequest.newInstance(
                                                prediction.placeId,
                                                listOf(Place.Field.FORMATTED_ADDRESS, Place.Field.DISPLAY_NAME),
                                            )
                                            val result = placesClient.fetchPlace(fetchRequest).await()
                                            val address = result.place.formattedAddress
                                                ?: result.place.displayName
                                                ?: prediction.getFullText(null).toString()
                                            onValueChange(address)
                                            onAddressSelected(address)
                                            predictions = emptyList()
                                        } catch (_: Exception) {
                                            // Fall back to the prediction text if fetch fails
                                            val fallback = prediction.getFullText(null).toString()
                                            onValueChange(fallback)
                                            onAddressSelected(fallback)
                                            predictions = emptyList()
                                        }
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Rounded.MyLocation,
                                contentDescription = null,
                                tint     = c.blue,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text  = prediction.getPrimaryText(null).toString(),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Medium,
                                        color      = c.textBright,
                                    ),
                                    maxLines = 1,
                                )
                                Text(
                                    text  = prediction.getSecondaryText(null).toString(),
                                    style = MaterialTheme.typography.bodySmall.copy(color = c.textMuted),
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

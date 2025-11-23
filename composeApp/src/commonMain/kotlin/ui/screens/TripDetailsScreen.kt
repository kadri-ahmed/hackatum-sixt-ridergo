package ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import ui.common.SectionHeader
import ui.common.SixtCard
import ui.common.SixtInput
import ui.common.SixtPrimaryButton
import ui.theme.SixtOrange
import viewmodels.SearchViewModel

@OptIn(KoinExperimentalAPI::class, ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class
)
@Composable
fun TripDetailsScreen(
    viewModel: SearchViewModel = koinViewModel(),
    onBack: () -> Unit,
    onSearch: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var destination by remember { mutableStateOf("") }
    var pickupDate by remember { mutableStateOf("Oct 24, 10:00 AM") }
    var returnDate by remember { mutableStateOf("Oct 27, 10:00 AM") }

    // Handle side effects
    LaunchedEffect(uiState) {
        if (uiState is ui.state.SearchUiState.Success) {
            val bookingId = (uiState as ui.state.SearchUiState.Success).bookingId
            onSearch(bookingId)
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trip Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                SixtCard {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        SectionHeader("Where to?")
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.LocationOn, contentDescription = null, tint = SixtOrange)
                            Spacer(modifier = Modifier.width(8.dp))
                            SixtInput(
                                value = destination,
                                onValueChange = { destination = it },
                                label = "Pick-up Location"
                            )
                        }

                        SectionHeader("When?")
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.DateRange, contentDescription = null, tint = SixtOrange)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                SixtInput(
                                    value = pickupDate,
                                    onValueChange = { pickupDate = it },
                                    label = "Pick-up"
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                SixtInput(
                                    value = returnDate,
                                    onValueChange = { returnDate = it },
                                    label = "Return"
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        SixtPrimaryButton(
                            text = "Update Trip",
                            onClick = { viewModel.createBooking(destination) },
                            enabled = uiState !is ui.state.SearchUiState.Loading
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Contextual Intelligence Teaser
                if (destination.isNotEmpty()) {
                    ContextualInsightCard(destination)
                }
            }
            
            // Loading Overlay
            if (uiState is ui.state.SearchUiState.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    ui.common.LoadingIndicator(message = "Updating your trip...")
                }
            }
            
            // Error Overlay
            if (uiState is ui.state.SearchUiState.Error) {
                val errorMsg = (uiState as ui.state.SearchUiState.Error).message
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    SixtCard(modifier = Modifier.padding(32.dp)) {
                        ui.common.ErrorView(
                            message = errorMsg,
                            onRetry = { viewModel.createBooking(destination) }
                        )
                    }
                }
            }
        }
    }
}

package ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import dto.Deal
import dto.Price
import dto.Pricing
import dto.Vehicle
import dto.VehicleAttribute
import dto.VehicleCost
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import ui.common.SixtCard
import ui.common.SixtPrimaryButton
import ui.state.VehicleListUiState
import ui.theme.SixtOrange
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

import viewmodels.VehicleListViewModel

@OptIn(ExperimentalMaterial3Api::class, KoinExperimentalAPI::class)
@Composable
fun VehicleListScreen(
    viewModel: viewmodels.VehicleListViewModel = koinViewModel(),
    onBack: () -> Unit,
    onVehicleSelected: (Deal) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // In a real app, we would get this ID from navigation arguments
    // For now, we trigger the load once
    LaunchedEffect(Unit) {
        // In a real scenario, this would be passed from SearchScreen
        viewModel.loadVehicles()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Choose your vehicle", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Oct 24, 10:00 AM - Oct 27, 10:00 AM",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (val state = uiState) {
                is ui.state.VehicleListUiState.Loading -> {
                    ui.common.LoadingIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        message = "Finding the best deals for you..."
                    )
                }
                is ui.state.VehicleListUiState.Error -> {
                    ui.common.ErrorView(
                        modifier = Modifier.align(Alignment.Center),
                        message = state.message,
                        onRetry = { viewModel.loadVehicles() }
                    )
                }
                is ui.state.VehicleListUiState.Empty -> {
                    ui.common.EmptyStateView(
                        modifier = Modifier.align(Alignment.Center),
                        message = "No vehicles available for these dates."
                    )
                }
                is ui.state.VehicleListUiState.Success -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // AI Recommendation Header
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.secondaryContainer,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "We found 3 perfect matches for your mountain trip!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }

                        items(state.deals) { deal ->
                            VehicleCard(deal = deal, onSelect = { onVehicleSelected(deal) })
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun VehicleCard(deal: Deal, onSelect: () -> Unit) {
    SixtCard(onClick = onSelect) {
        Column {
            // Header with Brand/Model and Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "${deal.vehicle.brand} ${deal.vehicle.model}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = deal.vehicle.groupType,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${deal.pricing.displayPrice.currency} ${deal.pricing.displayPrice.amount}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = SixtOrange,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = deal.pricing.displayPrice.suffix ?: "/day",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (deal.vehicle.images.isNotEmpty()) {
                AsyncImage(
                    model = deal.vehicle.images.first(),
                    contentDescription = "${deal.vehicle.brand} ${deal.vehicle.model}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No Image Available", color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Features
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                FeatureItem(icon = Icons.Filled.Person, text = "${deal.vehicle.passengersCount}")
                FeatureItem(icon = Icons.Filled.Settings, text = deal.vehicle.transmissionType)
                // Add more features as needed
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Recommendations / Upsell Reasons
            if (deal.vehicle.upsellReasons.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SixtOrange.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    deal.vehicle.upsellReasons.forEach { reason ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = null,
                                tint = SixtOrange,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = reason.title,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            SixtPrimaryButton(
                text = "Select",
                onClick = onSelect,
                modifier = Modifier.height(48.dp)
            )
        }
    }
}

@Composable
fun FeatureItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
    }
}

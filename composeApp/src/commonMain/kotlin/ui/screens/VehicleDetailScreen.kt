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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import dto.Deal
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dto.AddonCategory
import dto.ProtectionPackageDto
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import network.api.SixtApiImpl
import repositories.VehiclesRepository
import repositories.VehiclesRepositoryImpl
import ui.common.SixtCard
import ui.common.SixtPrimaryButton
import ui.theme.SixtOrange
import utils.Result

@Composable
fun VehicleDetailScreen(
    deal: Deal,
    onBack: () -> Unit,
    onUpgrade: (Deal) -> Unit
) {
    // Dependency Injection (Simplified)
    val client = remember { 
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                    isLenient = true
                })
            }
        } 
    }
    val api = remember { SixtApiImpl(client) }
    val repository = remember<VehiclesRepository> { VehiclesRepositoryImpl(api) }

    var protectionPackages by remember { mutableStateOf<List<ProtectionPackageDto>>(emptyList()) }
    var addons by remember { mutableStateOf<List<AddonCategory>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        // Fetch protections
        when (val result = repository.getAvailableProtectionPackages("mock_booking_id")) {
            is Result.Success -> protectionPackages = result.data.protectionPackages
            is Result.Error -> println("Error fetching protections")
        }
        // Fetch addons
        when (val result = repository.getAvailableAddons("mock_booking_id")) {
            is Result.Success -> addons = result.data.addons
            is Result.Error -> println("Error fetching addons")
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Vehicle Header
            Text(
                text = "${deal.vehicle.brand} ${deal.vehicle.model}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = deal.dealInfo ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Vehicle Image
            if (deal.vehicle.images.isNotEmpty()) {
                AsyncImage(
                    model = deal.vehicle.images.first(),
                    contentDescription = "${deal.vehicle.brand} ${deal.vehicle.model}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color.Gray, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Vehicle Image", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Features List
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FeatureRow("Efficient Performance", "Turbocharged power with 27 MPG for longer summer drives.")
                FeatureRow("Modern Comfort", "Keyless entry and smartphone integration for seamless journeys.")
                FeatureRow("Spacious Versatility", "Roomy interior with 5 doors for family adventures.")
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Protection Packages
            Text(
                text = "Protection Packages",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                protectionPackages.forEach { packageItem ->
                    SixtCard(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        onClick = { /* Select package */ }
                    ) {
                        Column {
                            Text(packageItem.name, fontWeight = FontWeight.Bold)
                            Text(packageItem.description ?: "", style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "${packageItem.price.displayPrice.currency} ${packageItem.price.displayPrice.amount} ${packageItem.price.displayPrice.suffix}",
                                color = SixtOrange,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Addons
            Text(
                text = "Extras",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (!isLoading) {
                addons.forEach { category ->
                    Text(category.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    category.options.forEach { option ->
                        SixtCard(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            onClick = { /* Select addon */ }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (option.chargeDetail.iconUrl != null) {
                                    AsyncImage(
                                        model = option.chargeDetail.iconUrl,
                                        contentDescription = null,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                }
                                Column {
                                    Text(option.chargeDetail.title, fontWeight = FontWeight.Bold)
                                    Text(option.chargeDetail.description, style = MaterialTheme.typography.bodySmall)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "${option.additionalInfo.price.displayPrice.currency} ${option.additionalInfo.price.displayPrice.amount} ${option.additionalInfo.price.displayPrice.suffix}",
                                        color = SixtOrange,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Price & Upgrade Button
            Text(
                text = "+ ${deal.pricing.displayPrice.currency}${deal.pricing.displayPrice.amount} /day",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            SixtPrimaryButton(
                text = "Continue with upgrade",
                onClick = { onUpgrade(deal) }
            )
        }
    }
}

@Composable
fun UpgradeCard(brand: String, model: String, price: String, imageUrl: String) {
    SixtCard(
        modifier = Modifier.width(200.dp),
        onClick = { /* Select upgrade */ }
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color.LightGray, RoundedCornerShape(8.dp))
            ) {
                 AsyncImage(
                    model = imageUrl,
                    contentDescription = "$brand $model",
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("$brand $model", fontWeight = FontWeight.Bold)
            Text(price, style = MaterialTheme.typography.bodySmall, color = SixtOrange)
        }
    }
}

@Composable
fun FeatureRow(title: String, description: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
import ui.theme.SixtOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleListScreen(
    onBack: () -> Unit,
    onVehicleSelected: (Deal) -> Unit
) {
    // Mock Data
    val deals = listOf(
        Deal(
            vehicle = Vehicle(
                id = "1",
                brand = "BMW",
                model = "X5",
                acrissCode = "XFAR",
                images = listOf("https://example.com/bmw_x5.png"),
                bagsCount = 3,
                passengersCount = 5,
                groupType = "SUV",
                tyreType = "Summer",
                transmissionType = "Automatic",
                fuelType = "Petrol",
                isNewCar = true,
                isRecommended = true,
                isMoreLuxury = true,
                isExcitingDiscount = false,
                attributes = emptyList(),
                vehicleStatus = "AVAILABLE",
                vehicleCost = VehicleCost("USD", 100),
                upsellReasons = listOf("Great for mountain terrain", "4WD for snow safety")
            ),
            pricing = Pricing(
                discountPercentage = 0,
                displayPrice = Price("USD", 120.0, suffix = "/day"),
                totalPrice = Price("USD", 120.0)
            ),
            dealInfo = "Premium SUV",
            tags = listOf("Mountain Ready", "Winter Safe")
        ),
        Deal(
            vehicle = Vehicle(
                id = "2",
                brand = "Mercedes-Benz",
                model = "C-Class",
                acrissCode = "FDAR",
                images = listOf("https://example.com/mercedes_c_class.png"),
                bagsCount = 2,
                passengersCount = 5,
                groupType = "Sedan",
                tyreType = "Summer",
                transmissionType = "Automatic",
                fuelType = "Petrol",
                isNewCar = false,
                isRecommended = false,
                isMoreLuxury = false,
                isExcitingDiscount = true,
                attributes = emptyList(),
                vehicleStatus = "AVAILABLE",
                vehicleCost = VehicleCost("USD", 80),
                upsellReasons = emptyList()
            ),
            pricing = Pricing(
                discountPercentage = 10,
                displayPrice = Price("USD", 90.0, suffix = "/day"),
                totalPrice = Price("USD", 90.0)
            ),
            dealInfo = "Comfort Sedan"
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Vehicle") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Recommended for your trip to the Mountains",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            items(deals) { deal ->
                VehicleCard(deal = deal, onSelect = { onVehicleSelected(deal) })
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

            // Vehicle Image Placeholder (In real app, use AsyncImage)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("Vehicle Image", color = Color.Gray)
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
                                text = reason,
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

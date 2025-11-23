package ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import dto.AddonCategory
import dto.Deal
import dto.ProtectionPackageDto
import repositories.VehiclesRepository
import ui.theme.SixtOrange
import utils.Result

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VehicleDetailScreen(
    deal: Deal,
    onBack: () -> Unit,
    onUpgrade: (Deal) -> Unit,
    bookingFlowViewModel: viewmodels.BookingFlowViewModel = org.koin.compose.koinInject()
) {
    // Dependency Injection
    val repository: VehiclesRepository = org.koin.compose.koinInject()

    var protectionPackages by remember { mutableStateOf<List<ProtectionPackageDto>>(emptyList()) }
    var addons by remember { mutableStateOf<List<AddonCategory>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedProtectionId by remember { mutableStateOf<String?>(null) }
    
    val bookingId = bookingFlowViewModel.bookingId.collectAsState().value

    LaunchedEffect(bookingId) {
        if (bookingId != null) {
            // Fetch protections
            when (val result = repository.getAvailableProtectionPackages(bookingId)) {
                is Result.Success -> {
                    protectionPackages = result.data.protectionPackages
                    // Select middle package by default if available
                    if (protectionPackages.size >= 2) {
                        selectedProtectionId = protectionPackages[1].id
                    } else if (protectionPackages.isNotEmpty()) {
                        selectedProtectionId = protectionPackages[0].id
                    }
                }
                is Result.Error -> println("Error fetching protections")
            }
            // Fetch addons
            when (val result = repository.getAvailableAddons(bookingId)) {
                is Result.Success -> addons = result.data.addons
                is Result.Error -> println("Error fetching addons")
            }
            isLoading = false
        }
    }

    Scaffold(
        bottomBar = {
            // Sticky Bottom Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Total",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${deal.pricing.displayPrice.currency}${deal.pricing.displayPrice.amount}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Button(
                        onClick = { 
                            if (selectedProtectionId != null) {
                                bookingFlowViewModel.setSelectedProtectionPackageId(selectedProtectionId!!)
                            }
                            bookingFlowViewModel.selectVehicle(deal.vehicle.id)
                            onUpgrade(deal) 
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp)
                    ) {
                        Text("Book Now", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // 1. Hero Section (Image Pager)
            Box(modifier = Modifier.height(300.dp)) {
                if (deal.vehicle.images.isNotEmpty()) {
                    val pagerState = rememberPagerState(pageCount = { deal.vehicle.images.size })
                    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                        AsyncImage(
                            model = deal.vehicle.images[page],
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    
                    // Pager Indicator
                    Row(
                        Modifier
                            .height(30.dp)
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(pagerState.pageCount) { iteration ->
                            val color = if (pagerState.currentPage == iteration) SixtOrange else Color.White.copy(alpha = 0.5f)
                            Box(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .size(8.dp)
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No Image Available")
                    }
                }

                // Floating Back Button
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.TopStart)
                        .background(Color.White.copy(alpha = 0.8f), CircleShape)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
                }
            }

            Column(modifier = Modifier.padding(24.dp)) {
                // Title & Price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${deal.vehicle.brand} ${deal.vehicle.model}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = deal.vehicle.groupType,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "${deal.pricing.displayPrice.currency}${deal.pricing.displayPrice.amount}/day",
                        style = MaterialTheme.typography.titleMedium,
                        color = SixtOrange,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 2. Quick Specs (Horizontal Chips)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SpecChip(icon = Icons.Default.Settings, text = "Auto")
                    SpecChip(icon = Icons.Default.Person, text = "${deal.vehicle.passengersCount} Seats")
                    SpecChip(icon = Icons.Default.LocalGasStation, text = "Diesel")
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 3. Smart Protection Selector
                Text(
                    text = "Protection",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        items(protectionPackages) { item ->
                            DetailProtectionCard(
                                item = item,
                                isSelected = item.id == selectedProtectionId,
                                onClick = { selectedProtectionId = item.id }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 4. Extras (Compact List)
                Text(
                    text = "Extras",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (!isLoading) {
                    addons.forEach { category ->
                        category.options.take(3).forEach { option ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (option.chargeDetail.iconUrl != null) {
                                        AsyncImage(
                                            model = option.chargeDetail.iconUrl,
                                            contentDescription = null,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(option.chargeDetail.title, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        "${option.additionalInfo.price.displayPrice.currency}${option.additionalInfo.price.displayPrice.amount}", 
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(
                                    Icons.Default.Check, 
                                    contentDescription = "Add",
                                    tint = MaterialTheme.colorScheme.primary, // Placeholder for selection state
                                    modifier = Modifier.alpha(0.3f) // Dimmed for now as we haven't implemented addon selection
                                )
                            }
                        }
                    }
                }
                
                // Extra padding for bottom bar
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UpgradeCard(brand: String, model: String, price: String, imageUrl: String) {
    // This function remains unchanged from the original file
    // It is not used in the new VehicleDetailScreen but is kept as per instructions
    // to preserve parts not explicitly removed.
    ui.common.SixtCard(
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
    // This function remains unchanged from the original file
    // It is not used in the new VehicleDetailScreen but is kept as per instructions
    // to preserve parts not explicitly removed.
    Row(verticalAlignment = Alignment.Top) {
        Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun SpecChip(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun DetailProtectionCard(
    item: ProtectionPackageDto,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) SixtOrange else Color.Transparent
    val backgroundColor = if (isSelected) SixtOrange.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
    
    Box(
        modifier = Modifier
            .width(160.dp)
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.description ?: "Basic coverage",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "+ ${item.price.displayPrice.currency}${item.price.displayPrice.amount}",
                style = MaterialTheme.typography.titleSmall,
                color = if (isSelected) SixtOrange else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        }
        
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(24.dp)
                    .background(SixtOrange, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
    }
}



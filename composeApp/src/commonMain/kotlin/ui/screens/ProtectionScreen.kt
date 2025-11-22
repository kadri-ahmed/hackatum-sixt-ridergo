package ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dto.DeductibleAmount
import dto.Price
import dto.ProtectionIncluded
import dto.ProtectionPackageDto
import dto.ProtectionPackagePrice
import ui.common.SixtPrimaryButton
import ui.theme.SixtOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProtectionScreen(
    onBack: () -> Unit,
    onConfirm: () -> Unit
) {
    // Mock Data
    val packages = listOf(
        ProtectionPackageDto(
            id = "1",
            name = "Basic Protection",
            deductibleAmount = DeductibleAmount("USD", 1000),
            ratingStars = 1,
            isPreviouslySelected = false,
            isSelected = false,
            isDeductibleAvailable = true,
            includes = listOf(
                ProtectionIncluded("1", "Third Party Liability", "Covers damages to others"),
                ProtectionIncluded("2", "Theft Protection", "Covers vehicle theft")
            ),
            price = ProtectionPackagePrice(0, Price("USD", 0.0, suffix = "/day"), null, Price("USD", 0.0)),
            isNudge = false
        ),
        ProtectionPackageDto(
            id = "2",
            name = "Smart Protection",
            deductibleAmount = DeductibleAmount("USD", 500),
            ratingStars = 3,
            isPreviouslySelected = false,
            isSelected = true,
            isDeductibleAvailable = true,
            includes = listOf(
                ProtectionIncluded("1", "Third Party Liability", "Covers damages to others"),
                ProtectionIncluded("2", "Theft Protection", "Covers vehicle theft"),
                ProtectionIncluded("3", "Collision Damage Waiver", "Reduced liability")
            ),
            price = ProtectionPackagePrice(0, Price("USD", 15.0, suffix = "/day"), null, Price("USD", 15.0)),
            isNudge = true
        ),
        ProtectionPackageDto(
            id = "3",
            name = "Premium Protection",
            deductibleAmount = DeductibleAmount("USD", 0),
            ratingStars = 5,
            isPreviouslySelected = false,
            isSelected = false,
            isDeductibleAvailable = true,
            includes = listOf(
                ProtectionIncluded("1", "Third Party Liability", "Covers damages to others"),
                ProtectionIncluded("2", "Theft Protection", "Covers vehicle theft"),
                ProtectionIncluded("3", "Collision Damage Waiver", "Zero liability"),
                ProtectionIncluded("4", "Tire & Glass", "Full coverage")
            ),
            price = ProtectionPackagePrice(0, Price("USD", 25.0, suffix = "/day"), null, Price("USD", 25.0)),
            isNudge = false
        )
    )

    var selectedPackageId by remember { mutableStateOf("2") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Protection & Extras") },
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
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Recommended for Winter Conditions",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                items(packages) { pkg ->
                    ProtectionCard(
                        pkg = pkg,
                        isSelected = pkg.id == selectedPackageId,
                        onSelect = { selectedPackageId = pkg.id }
                    )
                }
            }
            
            Column(modifier = Modifier.padding(16.dp)) {
                SixtPrimaryButton(
                    text = "Continue to Booking",
                    onClick = onConfirm
                )
            }
        }
    }
}

@Composable
fun ProtectionCard(
    pkg: ProtectionPackageDto,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val borderColor = if (isSelected) SixtOrange else Color.Transparent
    val borderWidth = if (isSelected) 2.dp else 0.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .border(borderWidth, borderColor, RoundedCornerShape(12.dp))
            .clickable { onSelect() }
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = pkg.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (isSelected) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = "Selected", tint = SixtOrange)
                } else {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .border(2.dp, Color.Gray, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Deductible: ${pkg.deductibleAmount.currency} ${pkg.deductibleAmount.value}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            pkg.includes.forEach { included ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp), tint = SixtOrange)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(included.title, style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (pkg.isNudge) {
                    Text(
                        text = "Recommended",
                        style = MaterialTheme.typography.labelMedium,
                        color = SixtOrange,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }
                
                Text(
                    text = "+ ${pkg.price.displayPrice.currency} ${pkg.price.displayPrice.amount} ${pkg.price.displayPrice.suffix ?: ""}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

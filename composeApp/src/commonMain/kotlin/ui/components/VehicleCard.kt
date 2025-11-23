package ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dto.Deal
import org.jetbrains.compose.resources.ExperimentalResourceApi
import ui.common.SixtCard
import ui.common.SixtPrimaryButton
import ui.theme.SixtOrange

import ui.common.getCurrencySymbol

@OptIn(ExperimentalResourceApi::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun VehicleCard(deal: Deal, onSelect: () -> Unit, onLongClick: (() -> Unit)? = null) {
    SixtCard(onClick = onSelect, onLongClick = onLongClick) {
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
                        text = "${getCurrencySymbol(deal.pricing.totalPrice.currency)}${ui.common.formatPrice(deal.pricing.totalPrice.amount)}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = SixtOrange,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Total",
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

            // Features (Dynamic from Attributes)
            if (deal.vehicle.attributes.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Display up to 3 attributes to fit in the card
                    deal.vehicle.attributes.take(3).forEach { attribute ->
                        AttributeItem(attribute)
                    }
                }
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
                                Icons.Default.Star,
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
fun AttributeItem(attribute: dto.VehicleAttribute) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (attribute.iconUrl != null) {
            AsyncImage(
                model = attribute.iconUrl,
                contentDescription = attribute.title,
                modifier = Modifier.size(16.dp),
                contentScale = ContentScale.Fit
            )
        } else {
            // Fallback icon if no URL
             Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(attribute.value, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
    }
}

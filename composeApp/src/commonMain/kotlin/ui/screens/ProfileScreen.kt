package ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import ui.common.SectionHeader
import ui.common.SixtCard
import ui.common.SixtPrimaryButton
import ui.theme.AppTheme
import ui.theme.SixtOrange

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfileScreen(
    id: Int,
    showDetails: Boolean,
    popBackStack: () -> Unit,
    popUpToLogin: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // User Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "John Doe",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Gold Member",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SixtOrange
                )
            }
        }

        // Settings
        SixtCard {
            SectionHeader("Settings")
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Dark Mode", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = { onToggleTheme() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = SixtOrange,
                        checkedTrackColor = SixtOrange.copy(alpha = 0.2f)
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Account Details
            Row(
                modifier = Modifier.fillMaxWidth().clickable { /* TODO: Navigate to Account Details */ },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Account Details", style = MaterialTheme.typography.bodyLarge)
                Icon(
                    imageVector = Icons.Default.Person, // Placeholder icon
                    contentDescription = "Account Details",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Support
            Row(
                modifier = Modifier.fillMaxWidth().clickable { /* TODO: Navigate to Support */ },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Support", style = MaterialTheme.typography.bodyLarge)
                 Icon(
                    imageVector = Icons.Default.Email, // Placeholder icon
                    contentDescription = "Support",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
             Spacer(modifier = Modifier.height(16.dp))

            // Version
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Version", style = MaterialTheme.typography.bodyLarge)
                Text("1.0.0", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Live Demo Settings
            val storage: utils.Storage = org.koin.compose.koinInject()
            var isLiveDemo by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(storage.getPreference("live_demo_enabled") == "true") }
            var apiKey by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(storage.getPreference("groq_api_key") ?: "") }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Live Demo", style = MaterialTheme.typography.bodyLarge)
                    Text("Enable custom API Key", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = isLiveDemo,
                    onCheckedChange = { 
                        isLiveDemo = it 
                        storage.savePreference("live_demo_enabled", it.toString())
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = SixtOrange,
                        checkedTrackColor = SixtOrange.copy(alpha = 0.2f)
                    )
                )
            }

            if (isLiveDemo) {
                Spacer(modifier = Modifier.height(8.dp))
                ui.common.SixtInput(
                    value = apiKey,
                    onValueChange = { 
                        apiKey = it
                    },
                    label = "Groq API Key"
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                ui.common.SixtPrimaryButton(
                    text = "Save API Key",
                    onClick = {
                        storage.savePreference("groq_api_key", apiKey)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        SixtPrimaryButton(
            text = "Log Out",
            onClick = popUpToLogin
        )
    }
}

// @Preview(showBackground = true)
// @Composable
// private fun ProfilePreview() {
//     AppTheme {
//         Surface(
//             modifier = Modifier.fillMaxSize(),
//             color = MaterialTheme.colorScheme.background
//         ) {
//             ProfileScreen(
//                 id = 7,
//                 showDetails = true,
//                 popBackStack = {},
//                 popUpToLogin = {}
//             )
//         }
//     }
// }
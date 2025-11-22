package ui.components

import ai.models.*
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ui.theme.SixtOrange

data class QuizQuestion(
    val question: String,
    val options: List<QuizOption>
)

data class QuizOption(
    val icon: ImageVector,
    val label: String,
    val answer: Any
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun QuizView(
    step: Int,
    onAnswer: (Any) -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val questions = remember {
        listOf(
            QuizQuestion(
                "Where are you headed?",
                listOf(
                    QuizOption(Icons.Default.LocationCity, "City", TerrainType.CITY),
                    QuizOption(Icons.Default.Landscape, "Mountains", TerrainType.MOUNTAIN),
                    QuizOption(Icons.Default.Directions, "Highway", TerrainType. HIGHWAY),
                    QuizOption(Icons.Default.Explore, "Mixed", TerrainType.MIXED)
                )
            ),
            QuizQuestion(
                "What's the weather like?",
                listOf(
                    QuizOption(Icons.Default.WbSunny, "Sunny", WeatherCondition.SUNNY),
                    QuizOption(Icons.Default.CloudQueue, "Rainy", WeatherCondition.RAINY),
                    QuizOption(Icons.Default.AcUnit, "Snowy", WeatherCondition.SNOWY),
                    QuizOption(Icons.Default.Cloud, "Mixed", WeatherCondition.MIXED)
                )
            ),
            QuizQuestion(
                "How many passengers?",
                listOf(
                    QuizOption(Icons.Default.Person, "Just me", 1),
                    QuizOption(Icons.Default.People, "2-3 people", 3),
                    QuizOption(Icons.Default.Groups, "4-5 people", 5),
                    QuizOption(Icons.Default.GroupWork, "6+ people", 7)
                )
            ),
            QuizQuestion(
                "How much luggage?",
                listOf(
                    QuizOption(Icons.Default.WorkOff, "None", 0),
                    QuizOption(Icons.Default.Work, "Small bag", 1),
                    QuizOption(Icons.Default.Luggage, "2-3 bags", 3),
                    QuizOption(Icons.Default.Inventory, "Lots of bags", 5)
                )
            ),
            QuizQuestion(
                "What's your trip purpose?",
                listOf(
                    QuizOption(Icons.Default.BusinessCenter, "Business", TripPurpose.BUSINESS),
                    QuizOption(Icons.Default.BeachAccess, "Leisure", TripPurpose.LEISURE),
                    QuizOption(Icons.Default.FamilyRestroom, "Family", TripPurpose.FAMILY),
                    QuizOption(Icons.Default.Hiking, "Adventure", TripPurpose.ADVENTURE)
                )
            ),
            QuizQuestion(
                "Trip duration?",
                listOf(
                    QuizOption(Icons.Default.Today, "1 day", 1),
                    QuizOption(Icons.Default.DateRange, "2-3 days", 3),
                    QuizOption(Icons.Default.CalendarMonth, "Week+", 7),
                    QuizOption(Icons.Default.Event, "Long trip", 14)
                )
            )
        )
    }
    
    val currentQuestion = questions.getOrNull(step)
    
    if (currentQuestion != null) {
        AnimatedContent(
            targetState = step,
            transitionSpec = {
                slideInHorizontally { width -> width } + fadeIn() with
                        slideOutHorizontally { width -> -width } + fadeOut()
            }
        ) { targetStep ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Progress indicator
                LinearProgressIndicator(
                    progress = (targetStep + 1) / questions.size.toFloat(),
                    modifier = Modifier.fillMaxWidth(),
                    color = SixtOrange
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Question
                Text(
                    currentQuestion.question,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(48.dp))
                
                // Options
                currentQuestion.options.forEach { option ->
                    QuizOptionCard(
                        icon = option.icon,
                        label = option.label,
                        onClick = { onAnswer(option.answer) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Skip button
                TextButton(onClick = onSkip) {
                    Text("Skip Quiz", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun QuizOptionCard(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = SixtOrange,
                modifier = Modifier.size(32.dp)
            )
            Text(
                label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

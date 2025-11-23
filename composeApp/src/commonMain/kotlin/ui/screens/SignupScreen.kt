package ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dto.UserProfile
import org.jetbrains.compose.resources.painterResource
import repositories.UserRepository
import ridergo.composeapp.generated.resources.Res
import ridergo.composeapp.generated.resources.cleveride_logo
import ui.common.SixtInput
import ui.theme.SixtOrange

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SignupScreen(
        onSignupComplete: () -> Unit,
        userRepository: UserRepository = org.koin.compose.koinInject()
) {
    var step by remember { mutableStateOf(0) }

    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var licenseType by remember { mutableStateOf("Automatic") }
    var travelPreference by remember { mutableStateOf("Leisure") }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { paddingValues ->
        Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Progress Indicator
            Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.Center
            ) {
                repeat(3) { index ->
                    Box(
                            modifier =
                                    Modifier.padding(4.dp)
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(
                                                    if (index <= step) SixtOrange
                                                    else MaterialTheme.colorScheme.surfaceVariant
                                            )
                    )
                }
            }

            AnimatedContent(
                    targetState = step,
                    modifier = Modifier.weight(1f),
                    transitionSpec = {
                        if (targetState > initialState) {
                            slideInHorizontally { width -> width } + fadeIn() with
                                    slideOutHorizontally { width -> -width } + fadeOut()
                        } else {
                            slideInHorizontally { width -> -width } + fadeIn() with
                                    slideOutHorizontally { width -> width } + fadeOut()
                        }
                    }
            ) { currentStep ->
                Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                ) {
                    when (currentStep) {
                        0 -> {
                            Image(
                                    painter = painterResource(Res.drawable.cleveride_logo),
                                    contentDescription = "Cleveride Logo",
                                    modifier = Modifier.height(80.dp),
                                    contentScale = ContentScale.Fit
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                    text = "Let's get to know you better.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(32.dp))

                            SixtInput(
                                    value = name,
                                    onValueChange = { name = it },
                                    label = "What's your name?",
                                    modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            SixtInput(
                                    value = age,
                                    onValueChange = {
                                        if (it.all { char -> char.isDigit() }) age = it
                                    },
                                    label = "How old are you?",
                                    keyboardOptions =
                                            KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth()
                            )
                        }
                        1 -> {
                            Text(
                                    text = "Driving Preference",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(32.dp))

                            SelectionButton(
                                    text = "Automatic",
                                    isSelected = licenseType == "Automatic",
                                    onClick = { licenseType = "Automatic" }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            SelectionButton(
                                    text = "Manual",
                                    isSelected = licenseType == "Manual",
                                    onClick = { licenseType = "Manual" }
                            )
                        }
                        2 -> {
                            Text(
                                    text = "Travel Style",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(32.dp))

                            SelectionButton(
                                    text = "Leisure / Vacation",
                                    isSelected = travelPreference == "Leisure",
                                    onClick = { travelPreference = "Leisure" }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            SelectionButton(
                                    text = "Business",
                                    isSelected = travelPreference == "Business",
                                    onClick = { travelPreference = "Business" }
                            )
                        }
                    }
                }
            }

            Button(
                    onClick = {
                        if (step < 2) {
                            if (step == 0 && (name.isBlank() || age.isBlank())) return@Button
                            step++
                        } else {
                            // Save and Finish
                            val profile = UserProfile(name, age, licenseType, travelPreference)
                            userRepository.saveProfile(profile)
                            onSignupComplete()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SixtOrange),
                    shape = RoundedCornerShape(16.dp),
                    enabled =
                            when (step) {
                                0 -> name.isNotBlank() && age.isNotBlank()
                                else -> true
                            }
            ) {
                Text(
                        text = if (step == 2) "Get Started" else "Next",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                )
                if (step < 2) {
                    Spacer(modifier = Modifier.size(8.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null)
                }
            }
        }
    }
}

@Composable
fun SelectionButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth().height(64.dp),
            colors =
                    ButtonDefaults.buttonColors(
                            containerColor =
                                    if (isSelected) SixtOrange.copy(alpha = 0.1f)
                                    else MaterialTheme.colorScheme.surface,
                            contentColor =
                                    if (isSelected) SixtOrange
                                    else MaterialTheme.colorScheme.onSurface
                    ),
            shape = RoundedCornerShape(16.dp),
            border =
                    if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, SixtOrange)
                    else null,
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
    ) {
        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
            if (isSelected) {
                Icon(Icons.Default.Check, contentDescription = null)
            }
        }
    }
}

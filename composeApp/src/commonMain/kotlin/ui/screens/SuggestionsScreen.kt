package ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import ui.animations.SuccessAnimation
import ui.common.LoadingIndicator
import ui.components.QuizView
import ui.components.SwipeableCard
import ui.state.SuggestionsUiState
import ui.theme.SixtOrange
import viewmodels.SuggestionsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuggestionsScreen(
    viewModel: SuggestionsViewModel = koinViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val quizStep by viewModel.quizStep.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Recommendations") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SixtOrange,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is SuggestionsUiState.Initial -> {
                    InitialView(onStartQuiz = { viewModel.startQuiz() })
                }
                is SuggestionsUiState.Quiz -> {
                    QuizView(
                        step = quizStep,
                        onAnswer = viewModel::answerQuestion,
                        onSkip = viewModel::skipQuiz
                    )
                }
                is SuggestionsUiState.Loading -> {
                    LoadingIndicator(message = "Finding your perfect match...")
                }
                is SuggestionsUiState.Success -> {
                    val currentCard = state.recommendations.getOrNull(state.currentIndex)
                    if (currentCard != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            SwipeableCard(
                                recommendation = currentCard,
                                onSwipeLeft = viewModel::swipeLeft,
                                onSwipeRight = { viewModel.swipeRight(currentCard) }
                            )
                        }
                    } else {
                        // All cards swiped
                        CompletionView(
                            likedCount = viewModel.getLikedVehicles().size,
                            onDone = onBack
                        )
                    }
                }
                is SuggestionsUiState.Error -> {
                    ErrorView(
                        message = state.message,
                        onRetry = { viewModel.startQuiz() }
                    )
                }
            }
        }
    }
}

@Composable
private fun InitialView(onStartQuiz: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.AutoAwesome,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = SixtOrange
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            "Find Your Perfect Car",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            "Answer a few quick questions and let our AI recommend the best vehicle for your trip!",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onStartQuiz,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SixtOrange)
        ) {
            Text("Start Quiz", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun CompletionView(
    likedCount: Int,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = SixtOrange
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            "All Done!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            "You liked $likedCount vehicle${if (likedCount != 1) "s" else ""}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = SixtOrange)
        ) {
            Text("Done")
        }
    }
}

@Composable
private fun ErrorView(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(onClick = onRetry) {
            Text("Try Again")
        }
    }
}

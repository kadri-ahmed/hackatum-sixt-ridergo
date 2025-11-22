package ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.ui.tooling.preview.Preview
import ui.common.SixtPrimaryButton
import ui.theme.AppTheme

@Composable
fun LoginScreen(
    navigateToHome: () -> Unit
) {
    Column (
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Login Screen", fontSize = 40.sp)

        SixtPrimaryButton(
            text = "Log In",
            onClick = navigateToHome
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginPreview() {
    AppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            LoginScreen(
                navigateToHome = {}
            )
        }
    }
}

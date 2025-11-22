package ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.ui.tooling.preview.Preview
import ui.common.SixtPrimaryButton
import ui.theme.AppTheme

@Composable
fun ProfileScreen(
    id: Int,
    showDetails: Boolean,
    popBackStack: () -> Unit,
    popUpToLogin: () -> Unit
) {
    Column (
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Profile Id: $id", fontSize = 40.sp)

        Spacer(modifier = Modifier.height(5.dp))
        Text("Details: $showDetails", fontSize = 40.sp)

        SixtPrimaryButton(
            text = "Back",
            onClick = popBackStack
        )

        SixtPrimaryButton(
            text = "Log Out",
            onClick = popUpToLogin
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfilePreview() {
    AppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            ProfileScreen(
                id = 7,
                showDetails = true,
                popBackStack = {},
                popUpToLogin = {}
            )
        }
    }
}
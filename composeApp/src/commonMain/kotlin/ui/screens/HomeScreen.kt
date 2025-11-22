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
fun HomeScreen(
    navigateToProfile: (Int, Boolean) -> Unit,
    navigateToSearch: (String) -> Unit,
    popBackStack: () -> Unit,
    popUpToLogin: () -> Unit,
) {

    Column (
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Home Screen", fontSize = 40.sp)

        SixtPrimaryButton(
            text = "Profile",
            onClick = { navigateToProfile(7, true) }
        )

        SixtPrimaryButton(
            text = "Search",
            onClick = { navigateToSearch("liang moi") }
        )

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
private fun HomePreview() {
    AppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            HomeScreen(
                navigateToProfile = { _,_ -> },
                navigateToSearch = {},
                popBackStack = {},
                popUpToLogin = {})
       }
    }
}
package com.warden.rnsl.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.warden.rnsl.ui.WardenViewModel
import com.warden.rnsl.ui.components.WardenTopBar
import com.warden.rnsl.ui.theme.WardenBlue

@Composable
fun AdbSetupScreen(viewModel: WardenViewModel, navController: NavController) {
    Column(modifier = Modifier.fillMaxSize()) {
        WardenTopBar(
            title = "ADB Setup",
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            }
        )
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(
                    Icons.Filled.Construction,
                    contentDescription = null,
                    tint = WardenBlue,
                    modifier = Modifier.size(72.dp)
                )
                Text(
                    text = "Under Development",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
                Text(
                    text = "ADB wireless pairing is coming in a future update. For now, please use Shizuku for live logging.",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = { navController.popBackStack() },
                    colors = ButtonDefaults.buttonColors(containerColor = WardenBlue)
                ) {
                    Text("Go Back")
                }
            }
        }
    }
}
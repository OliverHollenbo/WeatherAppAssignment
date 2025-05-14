package com.group13.weatherappfirstassignment.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryAdd
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun AppDrawer(
    navController: NavController,
    scope: CoroutineScope,
    scaffoldState: ScaffoldState,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.surface)
            .statusBarsPadding() // ✅ Handles top inset correctly
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(
            text = "DK Weather Observer",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colors.primary,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Divider(color = Color.LightGray)
        Spacer(modifier = Modifier.height(16.dp))

        DrawerButton("Home", Icons.Default.Home) {
            navController.navigate("home")
            scope.launch { scaffoldState.drawerState.close() }
        }

        DrawerButton("Nearby Stations", Icons.Default.LocationOn) {
            navController.navigate("nearby")
            scope.launch { scaffoldState.drawerState.close() }
        }

        DrawerButton("Favorite Stations", Icons.Default.LibraryAdd) {
            navController.navigate("favorites")
            scope.launch { scaffoldState.drawerState.close() }
        }

        Spacer(modifier = Modifier.weight(1f))

        Divider(color = Color.LightGray)
        Spacer(modifier = Modifier.height(12.dp))

        DrawerButton("Logout", Icons.Default.Logout) {
            onLogout()
            scope.launch { scaffoldState.drawerState.close() }
        }
    }
}

@Composable
private fun DrawerButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colors.onSurface)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = text)
            Spacer(modifier = Modifier.width(12.dp))
            Text(text, fontSize = 16.sp)
        }
    }
}

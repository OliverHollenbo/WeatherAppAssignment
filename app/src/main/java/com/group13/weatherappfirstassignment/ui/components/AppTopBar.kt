package com.group13.weatherappfirstassignment.ui.components

import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun AppTopBar(
    title: String,
    onMenuClick: () -> Unit,
    onLogoutClick: () -> Unit = {},
    actions: List<Pair<ImageVector, () -> Unit>> = emptyList()
) {
    TopAppBar(
        title = { Text(title) },
        modifier = Modifier.statusBarsPadding(),
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Menu")
            }
        },
        actions = {
            for ((icon, action) in actions) {
                IconButton(onClick = action) {
                    Icon(icon, contentDescription = null)
                }
            }
            IconButton(onClick = onLogoutClick) {
                Icon(Icons.Default.Logout, contentDescription = "Logout")
            }
        },
        elevation = 4.dp
    )
}

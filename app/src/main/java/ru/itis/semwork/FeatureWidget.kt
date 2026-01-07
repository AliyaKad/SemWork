package ru.itis.semwork

import androidx.compose.ui.graphics.vector.ImageVector

data class FeatureWidget(
    val id: Int,
    val title: String,
    val description: String,
    val iconVector: ImageVector,
    val route: String
)

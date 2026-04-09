package com.example.countdowntimer.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector

fun categoryIconFor(category: String): ImageVector = when (category) {
    "Birthday" -> Icons.Default.Cake
    "Anniversary" -> Icons.Default.Favorite
    "Running Events" -> Icons.AutoMirrored.Filled.DirectionsRun
    "Deadlines" -> Icons.AutoMirrored.Filled.Assignment
    "Exams", "Exam" -> Icons.Default.School
    "Work" -> Icons.Default.Work
    "Travel" -> Icons.Default.Flight
    "Health" -> Icons.Default.LocalHospital
    "Fitness" -> Icons.AutoMirrored.Filled.DirectionsRun
    "Finance" -> Icons.Default.Wallet
    "Holiday" -> Icons.Default.Celebration
    "Personal" -> Icons.Default.Person
    else -> Icons.Default.Category
}

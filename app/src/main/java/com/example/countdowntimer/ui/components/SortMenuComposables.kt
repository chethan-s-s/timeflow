package com.example.countdowntimer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SortMenuItemContent(
    label: String,
    description: String,
    isSelected: Boolean,
    isDark: Boolean
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(
                        color = if (isSelected)
                            (if (isDark) Color(0xFF90CAF9) else Color(0xFF1976D2))
                        else Color.Transparent,
                        shape = CircleShape
                    )
                    .border(
                        width = if (isSelected) 0.dp else 1.5.dp,
                        color = if (isDark) Color(0xFF666666) else Color(0xFFCCCCCC),
                        shape = CircleShape
                    )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = label,
                    color = if (isDark) Color(0xFFE0E0E0) else Color(0xFF333333),
                    fontSize = androidx.compose.ui.unit.TextUnit(14f, androidx.compose.ui.unit.TextUnitType.Sp),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    color = if (isDark) Color(0xFF999999) else Color(0xFF666666),
                    fontSize = androidx.compose.ui.unit.TextUnit(12f, androidx.compose.ui.unit.TextUnitType.Sp)
                )
            }
        }
    }
}

@Composable
fun SortMenuItemBackgroundModifier(
    isSelected: Boolean,
    isDark: Boolean
): Modifier {
    return Modifier
        .background(
            color = if (isSelected)
                (if (isDark) Color(0xFF263238).copy(alpha = 0.4f) else Color(0xFFE3F2FD).copy(alpha = 0.5f))
            else Color.Transparent
        )
        .padding(horizontal = 12.dp, vertical = 8.dp)
}



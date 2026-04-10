package com.chethans.timeflow.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chethans.timeflow.R

enum class LegalTab { PRIVACY_POLICY, TERMS_OF_SERVICE, ABOUT_DEVELOPER }

/**
 * A reusable dialog that displays Privacy Policy, Terms of Service, or About Developer content.
 *
 * @param tab     Which legal page to display.
 * @param onDismiss Called when the user closes the dialog.
 */
@Composable
fun LegalInfoDialog(
    tab: LegalTab,
    onDismiss: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val titleColor = if (isDark) Color(0xFF90CAF9) else Color(0xFF1565C0)
    val bodyColor = if (isDark) Color(0xFFCFCFCF) else Color(0xFF333333)
    val bgColor = if (isDark) Color(0xFF1E1E2E) else Color(0xFFFDFDFD)

    val title = when (tab) {
        LegalTab.PRIVACY_POLICY     -> stringResource(R.string.privacy_policy)
        LegalTab.TERMS_OF_SERVICE   -> stringResource(R.string.terms_of_service)
        LegalTab.ABOUT_DEVELOPER    -> stringResource(R.string.about_developer)
    }

    val body = when (tab) {
        LegalTab.PRIVACY_POLICY     -> stringResource(R.string.privacy_policy_content)
        LegalTab.TERMS_OF_SERVICE   -> stringResource(R.string.terms_of_service_content)
        LegalTab.ABOUT_DEVELOPER    -> stringResource(R.string.about_developer_content)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = bgColor,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = titleColor
                )
            )
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(end = 4.dp)
                ) {
                    Text(
                        text = body,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = bodyColor,
                            lineHeight = 22.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = stringResource(R.string.close),
                        color = titleColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    )
}



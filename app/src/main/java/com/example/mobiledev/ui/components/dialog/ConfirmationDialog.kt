package com.example.mobiledev.ui.components.dialog

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import com.example.mobiledev.ui.components.GlassyCard

/**
 * A reusable confirmation dialog for destructive or important actions.
 *
 * @param title The title of the dialog.
 * @param message The descriptive message explaining the action.
 * @param confirmButtonText The text for the confirmation button (defaults to "Confirm").
 * @param dismissButtonText The text for the dismissal button (defaults to "Cancel").
 * @param isDestructive If true, the confirmation button will use the error color (red).
 * @param onConfirm Callback when the confirm button is clicked.
 * @param onDismiss Callback when the dismiss button is clicked or the dialog is dismissed.
 */
@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmButtonText: String = "Confirm",
    dismissButtonText: String = "Cancel",
    isDestructive: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        GlassyCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f) // Slightly more opaque for readability in dialogs
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(text = dismissButtonText)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    androidx.compose.material3.Button(
                        onClick = onConfirm,
                        colors = if (isDestructive) {
                            androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFC61111),
                                contentColor = Color.White
                            )
                        } else {
                            androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00695C),
                                contentColor = Color.White
                            )
                        }
                    ) {
                        Text(text = confirmButtonText)
                    }
                }
            }
        }
    }
}

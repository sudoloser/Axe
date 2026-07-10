package com.my.axe.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.my.axe.resources.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

fun Int.formatSize(): String =
    (this / 1024f / 1024f)
        .takeIf { it > 0f }
        ?.run { " ${String.format("%.2f", this)} MB" } ?: ""

@Composable
fun UpdateDialog(
    modifier: Modifier = Modifier,
    newVersionPublishDate: String,
    newVersionSize: Int,
    newVersionLog: String,
    downloadUrl: String?,
    onDismissRequest: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isDownloading by remember { mutableStateOf(false) }

    AlertDialog(
        modifier = modifier,
        onDismissRequest = {
            if (!isDownloading) onDismissRequest()
        },
        icon = {
            Icon(
                imageVector = Icons.Outlined.Update,
                contentDescription = "Update",
            )
        },
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(text = stringResource(R.string.change_log))
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "$newVersionPublishDate ${newVersionSize.formatSize()}",
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        },
        text = {
            SelectionContainer {
                Text(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    text = newVersionLog,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (downloadUrl != null) {
                        isDownloading = true
                        scope.launch {
                            try {
                                val file = withContext(Dispatchers.IO) {
                                    val outputFile = File(context.filesDir, "axe_update.apk")
                                    val url = URL(downloadUrl)
                                    url.openStream().use { input ->
                                        FileOutputStream(outputFile).use { output ->
                                            input.copyTo(output)
                                        }
                                    }
                                    outputFile
                                }
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "com.my.axe.provider",
                                    file
                                )
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(uri, "application/vnd.android.package-archive")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {
                            }
                            isDownloading = false
                            onDismissRequest()
                        }
                    }
                },
                enabled = !isDownloading
            ) {
                Text(
                    text = if (isDownloading) "Downloading..." else stringResource(R.string.update)
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest,
                enabled = !isDownloading
            ) {
                Text(text = stringResource(R.string.cancel))
            }
        },
    )
}

@Preview
@Composable
fun UpdateDialogPreview() {
    UpdateDialog(
        newVersionLog = "1. Fix bugs\n2. Fix bugs\n3. Fix bugs",
        newVersionPublishDate = "2021-10-10",
        newVersionSize = 1000000,
        downloadUrl = null,
        onDismissRequest = {},
        modifier = Modifier
            .height(500.dp)
            .width(300.dp)
    )
}

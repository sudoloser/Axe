package com.my.axe.ui.components

import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.my.axe.data.utils.MarkdownParser
import com.my.axe.resources.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

private const val CHANGELOG_URL = "https://raw.githubusercontent.com/sudoloser/Axe/refs/heads/master/CHANGELOG.md"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangelogSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
) {
    if (!visible) return

    var changelogHtml by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(visible) {
        isLoading = true
        errorMessage = null
        try {
            val raw = withContext(Dispatchers.IO) {
                URL(CHANGELOG_URL).readText()
            }
            changelogHtml = MarkdownParser.toHtml(raw)
        } catch (e: Exception) {
            errorMessage = e.message ?: "Failed to load changelog"
        } finally {
            isLoading = false
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = stringResource(R.string.change_log),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                errorMessage != null -> {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
                changelogHtml != null -> {
                    AndroidView(
                        factory = { context ->
                            WebView(context).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT
                                )
                                webViewClient = WebViewClient()
                                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                                loadDataWithBaseURL(
                                    null,
                                    changelogHtml!!,
                                    "text/html",
                                    "UTF-8",
                                    null
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(500.dp)
                    )
                }
            }
        }
    }
}

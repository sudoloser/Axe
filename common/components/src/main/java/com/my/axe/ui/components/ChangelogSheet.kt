package com.my.axe.ui.components

import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.my.axe.resources.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

private const val CHANGELOG_URL = "https://raw.githubusercontent.com/sudoloser/Axe/refs/heads/master/CHANGELOG.md"

private object MarkdownParser {
    fun toHtml(markdown: String): String {
        var html = markdown
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")

        html = html.replace(Regex("""(?m)^(#{6})\s+(.*?)$""")) { "<h6>${it.groupValues[2]}</h6>" }
        html = html.replace(Regex("""(?m)^(#{5})\s+(.*?)$""")) { "<h5>${it.groupValues[2]}</h5>" }
        html = html.replace(Regex("""(?m)^(#{4})\s+(.*?)$""")) { "<h4>${it.groupValues[2]}</h4>" }
        html = html.replace(Regex("""(?m)^(#{3})\s+(.*?)$""")) { "<h3>${it.groupValues[2]}</h3>" }
        html = html.replace(Regex("""(?m)^(#{2})\s+(.*?)$""")) { "<h2>${it.groupValues[2]}</h2>" }
        html = html.replace(Regex("""(?m)^(#{1})\s+(.*?)$""")) { "<h1>${it.groupValues[2]}</h1>" }
        html = html.replace(Regex("""```([\s\S]*?)```""")) { "<pre><code>${it.groupValues[1].trim()}</code></pre>" }
        html = html.replace(Regex("""(?m)^\s*[-*+]\s+(.*?)$""")) { "<li>${it.groupValues[1]}</li>" }
        html = html.replace(Regex("""(?m)^\s*\d+\.\s+(.*?)$""")) { "<li>${it.groupValues[1]}</li>" }
        html = html.replace(Regex("""\[([^\]]+)\]\(([^)]+)\)""")) { "<a href=\"${it.groupValues[2]}\">${it.groupValues[1]}</a>" }
        html = html.replace(Regex("""https?://[^\s<]+""")) { "<a href=\"${it.value}\">${it.value}</a>" }
        html = html.replace(Regex("""(?m)^---+\s*$"""), "<hr>")
        html = html.replace(Regex("""\*\*(.*?)\*\*""")) { "<b>${it.groupValues[1]}</b>" }
        html = html.replace(Regex("""\*(.*?)\*""")) { "<i>${it.groupValues[1]}</i>" }
        html = html.replace(Regex("""`([^`]+)`""")) { "<code>${it.groupValues[1]}</code>" }
        html = html.replace(Regex("""\n{2,}"""), "</p><p>")
        html = html.replace(Regex("""(?m)^(?!<h[1-6]|<li>|<pre|<hr|<p>)(.+)$""")) { "<p>${it.groupValues[1]}</p>" }

        return """
<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<style>
body {
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
    padding: 16px;
    color: #e0e0e0;
    background: transparent;
    line-height: 1.6;
}
h1 { font-size: 24px; margin: 16px 0 8px; color: #ffffff; }
h2 { font-size: 20px; margin: 14px 0 6px; color: #ffffff; }
h3 { font-size: 18px; margin: 12px 0 4px; color: #ffffff; }
h4, h5, h6 { font-size: 16px; margin: 10px 0 4px; color: #ffffff; }
p { margin: 8px 0; }
ul, ol { padding-left: 20px; }
li { margin: 4px 0; }
a { color: #82b1ff; }
code {
    background: #2d2d2d;
    padding: 2px 6px;
    border-radius: 4px;
    font-size: 0.9em;
}
pre {
    background: #2d2d2d;
    padding: 12px;
    border-radius: 8px;
    overflow-x: auto;
}
pre code { background: none; padding: 0; border-radius: 0; }
hr { border: none; border-top: 1px solid #444; margin: 16px 0; }
</style>
</head>
<body>
${html.trim()}
</body>
</html>
""".trimIndent()
    }
}

private fun extractLatestSection(markdown: String): String {
    val lines = markdown.lines()
    val firstHeaderIndex = lines.indexOfFirst { it.startsWith("# ") }
    if (firstHeaderIndex == -1) return markdown

    val secondHeaderIndex = lines.indexOfFirst { it.startsWith("# ") && lines.indexOf(it) > firstHeaderIndex }
    return if (secondHeaderIndex == -1) {
        lines.subList(firstHeaderIndex, lines.size).joinToString("\n")
    } else {
        lines.subList(firstHeaderIndex, secondHeaderIndex).joinToString("\n")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangelogSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
) {
    if (!visible) return

    val context = LocalContext.current
    var changelogHtml by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(visible) {
        if (visible) {
            isLoading = true
            errorMessage = null
            try {
                val raw = withContext(Dispatchers.IO) {
                    URL(CHANGELOG_URL).readText()
                }
                val latest = extractLatestSection(raw)
                changelogHtml = MarkdownParser.toHtml(latest)
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to load changelog"
            } finally {
                isLoading = false
            }
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
                        factory = { ctx ->
                            WebView(ctx).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT
                                )
                                webViewClient = object : WebViewClient() {
                                    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                        return true
                                    }
                                }
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

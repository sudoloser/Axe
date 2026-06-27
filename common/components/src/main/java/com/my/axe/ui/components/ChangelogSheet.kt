package com.my.axe.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.my.axe.resources.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

private const val CHANGELOG_URL =
    "https://raw.githubusercontent.com/sudoloser/Axe/refs/heads/master/CHANGELOG.md"

private data class ChangelogSection(
    val header: String,
    val body: String,
) {
    val displayName: String get() = header.removePrefix("# ").trim()
}

private data class MarkdownStyle(
    val text: Color,
    val link: Color,
    val codeText: Color,
    val codeBg: Color,
    val quote: Color,
)

private fun parseSections(markdown: String): List<ChangelogSection> {
    val lines = markdown.lines()
    val sections = mutableListOf<ChangelogSection>()
    var currentHeader: String? = null
    val currentBody = mutableListOf<String>()

    for (line in lines) {
        if (line.startsWith("# ")) {
            if (currentHeader != null) {
                sections.add(ChangelogSection(currentHeader, currentBody.joinToString("\n")))
            }
            currentHeader = line
            currentBody.clear()
        } else if (currentHeader != null) {
            currentBody.add(line)
        }
    }
    if (currentHeader != null) {
        sections.add(ChangelogSection(currentHeader, currentBody.joinToString("\n")))
    }
    return sections
}

private data class TextRun(
    val text: String,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val code: Boolean = false,
    val link: String? = null,
)

private fun parseInline(markdown: String): List<TextRun> {
    val runs = mutableListOf<TextRun>()
    val pattern =
        Regex("""(\*\*(.+?)\*\*)|(\*(.+?)\*)|(`([^`]+)`)|(\[([^\]]+)\]\(([^)]+)\))|(https?://[^\s<"]+)""")
    var pos = 0
    for (m in pattern.findAll(markdown)) {
        if (m.range.first > pos) {
            runs.add(TextRun(markdown.substring(pos, m.range.first)))
        }
        when {
            m.groupValues[1].isNotEmpty() -> runs.add(TextRun(m.groupValues[2], bold = true))
            m.groupValues[3].isNotEmpty() -> runs.add(TextRun(m.groupValues[4], italic = true))
            m.groupValues[5].isNotEmpty() -> runs.add(TextRun(m.groupValues[6], code = true))
            m.groupValues[7].isNotEmpty() -> runs.add(TextRun(m.groupValues[8], link = m.groupValues[9]))
            m.groupValues[10].isNotEmpty() -> runs.add(TextRun(m.groupValues[10], link = m.groupValues[10]))
        }
        pos = m.range.last + 1
    }
    if (pos < markdown.length) {
        runs.add(TextRun(markdown.substring(pos)))
    }
    return runs
}

private fun AnnotatedString.Builder.appendRun(run: TextRun, style: MarkdownStyle) {
    val span = SpanStyle(
        fontFamily = if (run.code) FontFamily.Monospace else null,
        fontWeight = if (run.bold) FontWeight.Bold else null,
        fontStyle = if (run.italic) FontStyle.Italic else null,
        color = when {
            run.link != null -> style.link
            run.code -> style.codeText
            else -> Color.Unspecified
        },
        background = if (run.code) style.codeBg else Color.Unspecified,
        textDecoration = if (run.link != null) TextDecoration.Underline else null,
    )
    if (run.link != null) {
        pushStringAnnotation("URL", run.link)
        withStyle(span) { append(run.text) }
        pop()
    } else {
        withStyle(span) { append(run.text) }
    }
}

private fun markdownToAnnotatedString(markdown: String, style: MarkdownStyle): AnnotatedString = buildAnnotatedString {
    withStyle(SpanStyle(color = style.text)) {
        val lines = markdown.lines()
        var i = 0
        while (i < lines.size) {
            val line = lines[i]
            when {
                line.isBlank() -> { append("\n"); i++; continue }

                line.trimStart().startsWith("```") -> {
                    val code = mutableListOf<String>()
                    i++
                    while (i < lines.size && !lines[i].trimStart().startsWith("```")) {
                        code.add(lines[i])
                        i++
                    }
                    i++
                    withStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp,
                            color = style.codeText,
                            background = style.codeBg,
                        )
                    ) { append(code.joinToString("\n")) }
                    append("\n"); continue
                }

                line.matches(Regex("""---+\s*""")) -> {
                    append("\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\n"); i++; continue
                }

                line.startsWith("### ") -> {
                    withStyle(SpanStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold)) {
                        append(line.removePrefix("### "))
                    }
                    append("\n"); i++; continue
                }

                line.startsWith("## ") -> {
                    withStyle(SpanStyle(fontSize = 17.sp, fontWeight = FontWeight.Bold)) {
                        append(line.removePrefix("## "))
                    }
                    append("\n"); i++; continue
                }

                line.startsWith("# ") -> {
                    withStyle(SpanStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)) {
                        append(line.removePrefix("# "))
                    }
                    append("\n"); i++; continue
                }

                line.matches(Regex("""^\s*[-*+]\s+.*""")) -> {
                    append("  \u2022 ")
                    parseInline(line.trimStart().substringAfter(" ").trim()).forEach { appendRun(it, style) }
                    append("\n"); i++; continue
                }

                line.matches(Regex("""^\s*\d+\.\s+.*""")) -> {
                    append("  \u2022 ")
                    parseInline(line.trimStart().substringAfter(".").trim()).forEach { appendRun(it, style) }
                    append("\n"); i++; continue
                }

                line.startsWith("> ") -> {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = style.quote)) {
                        append("  \u2502 ")
                        append(line.removePrefix("> "))
                    }
                    append("\n"); i++; continue
                }

                else -> {
                    parseInline(line).forEach { appendRun(it, style) }
                    append("\n"); i++; continue
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangelogSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
) {
    if (!visible) return

    val uriHandler = LocalUriHandler.current
    var sections by remember { mutableStateOf<List<ChangelogSection>>(emptyList()) }
    var selectedIndex by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val sheetState = rememberModalBottomSheetState()
    val scrollState = rememberScrollState()

    LaunchedEffect(visible) {
        if (visible) {
            isLoading = true
            errorMessage = null
            try {
                val raw = withContext(Dispatchers.IO) { URL(CHANGELOG_URL).readText() }
                sections = parseSections(raw)
                selectedIndex = 0
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.change_log),
                    style = MaterialTheme.typography.headlineSmall,
                )

                if (sections.size > 1) {
                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        Row(
                            modifier = Modifier.clickable { expanded = true },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = sections.getOrNull(selectedIndex)?.displayName ?: "",
                                style = MaterialTheme.typography.bodySmall,
                            )
                            Spacer(Modifier.width(2.dp))
                            Text(
                                text = "\u25BC",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                        ) {
                            sections.forEachIndexed { index, section ->
                                DropdownMenuItem(
                                    text = { Text(section.displayName) },
                                    onClick = { selectedIndex = index; expanded = false },
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
                errorMessage != null -> {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = 16.dp),
                    )
                }
                sections.isNotEmpty() -> {
                    val colors = MaterialTheme.colorScheme
                    val mdStyle = remember(colors) {
                        MarkdownStyle(
                            text = colors.onSurface,
                            link = colors.primary,
                            codeText = colors.onSurfaceVariant,
                            codeBg = colors.surfaceVariant,
                            quote = colors.onSurfaceVariant,
                        )
                    }
                    val annotated = remember(sections, selectedIndex) {
                        val section = sections[selectedIndex]
                        markdownToAnnotatedString(section.header + "\n" + section.body, mdStyle)
                    }
                    SelectionContainer {
                        ClickableText(
                            text = annotated,
                            onClick = { offset ->
                                annotated.getStringAnnotations("URL", offset, offset)
                                    .firstOrNull()?.let { uriHandler.openUri(it.item) }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(scrollState),
                        )
                    }
                }
            }
        }
    }
}

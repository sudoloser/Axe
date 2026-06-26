package com.my.axe.data.utils

object MarkdownParser {

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

        html = html.replace(Regex("""(?m)^---+\s*$"""), "<hr>")

        html = html.replace(Regex("""\*\*(.*?)\*\*""")) { "<b>${it.groupValues[1]}</b>" }
        html = html.replace(Regex("""\*(.*?)\*""")) { "<i>${it.groupValues[1]}</i>" }
        html = html.replace(Regex("""`([^`]+)`""")) { "<code>${it.groupValues[1]}</code>" }

        html = html.replace(Regex("""\n{2,}"""), "</p><p>")
        html = html.replace(Regex("""(?m)^(?!<h[1-6]|<li>|<pre|<hr|<p>)(.+)$""")) { "<p>${it.groupValues[1]}</p>" }

        html = html.replace("</p><p>", "</p>\n<p>")
        html = html.replace(Regex("""<li>(.*?)</li>""")) { "<li>${it.groupValues[1]}</li>" }

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

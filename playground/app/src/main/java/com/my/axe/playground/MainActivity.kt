package com.my.axe.playground

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Uncodexify: Monochrome colors
private val BgColor = Color(0xFF0D1117)
private val CardBg = Color(0xFF161B22)
private val BorderColor = Color(0xFF30363D)
private val PrimaryColor = Color(0xFFFFFFFF)
private val TextMain = Color(0xFFE6EDF3)
private val TextMuted = Color(0xFF848D97)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PlaygroundTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BgColor
                ) {
                    PlaygroundScreen()
                }
            }
        }
    }
}

@Composable
fun PlaygroundTheme(content: @Composable () -> Unit) {
    val colorScheme = darkColorScheme(
        primary = PrimaryColor,
        background = BgColor,
        surface = CardBg,
        onPrimary = Color.Black,
        onBackground = TextMain,
        onSurface = TextMain,
        outline = BorderColor
    )
    MaterialTheme(colorScheme = colorScheme, content = content)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaygroundScreen() {
    var activityName by remember { mutableStateOf("Axe Playground") }
    var details by remember { mutableStateOf("Testing the API") }
    var state by remember { mutableStateOf("Level 1: Uncodexify") }
    var largeImage by remember { mutableStateOf("https://raw.githubusercontent.com/sudoloser/axe/master/axe.png") }
    var smallImage by remember { mutableStateOf("") }
    var applicationId by remember { mutableStateOf("") }
    var activityType by remember { mutableStateOf("playing") }

    val context = androidx.compose.ui.platform.LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Axe Intent Playground", fontWeight = FontWeight.SemiBold, fontSize = 20.sp) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BgColor,
                    titleContentColor = PrimaryColor
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "Use this app to test how Axe handles incoming intents. Ensure Axe is installed and 'Allow external apps' is enabled.",
                    color = TextMuted,
                    fontSize = 14.sp
                )
            }

            item {
                PlaygroundInput(label = "Activity Name", value = activityName) { activityName = it }
            }
            item {
                PlaygroundInput(label = "Details", value = details) { details = it }
            }
            item {
                PlaygroundInput(label = "State", value = state) { state = it }
            }
            item {
                PlaygroundInput(label = "Large Image URL", value = largeImage) { largeImage = it }
            }
            item {
                PlaygroundInput(label = "Small Image URL", value = smallImage) { smallImage = it }
            }
            item {
                PlaygroundInput(label = "Application ID (Optional)", value = applicationId) { applicationId = it }
            }
            item {
                PlaygroundInput(label = "Activity Type (playing, listening, etc.)", value = activityType) { activityType = it }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            sendUpdateIntent(context, activityName, details, state, largeImage, smallImage, applicationId, activityType)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor, contentColor = Color.Black)
                    ) {
                        Text("Update Presence", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { sendClearIntent(context) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMain)
                    ) {
                        Text("Clear Presence")
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaygroundInput(label: String, value: String, onValueChange: (String) -> Unit) {
    Column {
        Text(label, color = TextMuted, fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = PrimaryColor,
                unfocusedBorderColor = BorderColor,
                cursorColor = PrimaryColor,
                focusedLabelColor = PrimaryColor
            ),
            singleLine = true
        )
    }
}

fun sendUpdateIntent(
    context: Context,
    name: String,
    details: String,
    state: String,
    largeImg: String,
    smallImg: String,
    appId: String,
    type: String
) {
    val intent = Intent("com.my.axe.UPDATE_PRESENCE").apply {
        setPackage("com.my.axe")
        putExtra("activity_name", name)
        putExtra("details", details)
        putExtra("state", state)
        putExtra("large_image", largeImg)
        putExtra("small_image", smallImg)
        putExtra("application_id", appId)
        putExtra("activity_type", type)
        putExtra("source_package", context.packageName)
        putExtra("timestamp_start", System.currentTimeMillis())
    }
    context.sendBroadcast(intent)
    Toast.makeText(context, "Intent Sent", Toast.LENGTH_SHORT).show()
}

fun sendClearIntent(context: Context) {
    val intent = Intent("com.my.axe.CLEAR_PRESENCE").apply {
        setPackage("com.my.axe")
    }
    context.sendBroadcast(intent)
    Toast.makeText(context, "Clear Intent Sent", Toast.LENGTH_SHORT).show()
}

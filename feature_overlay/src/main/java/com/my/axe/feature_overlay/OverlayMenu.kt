package com.my.axe.feature_overlay

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.my.axe.data.utils.ConfigUtils
import com.my.axe.feature_rpc_base.AppUtils
import com.my.axe.feature_rpc_base.services.CustomRpcService
import com.my.axe.preference.Prefs

@Composable
fun OverlayMenu(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isRunning by remember { mutableStateOf(AppUtils.customRpcRunning()) }
    val configs = remember { ConfigUtils.getAllConfigs(context) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Axe Quick Controls",
                        style = MaterialTheme.typography.titleLarge
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

import androidx.compose.material.icons.filled.Settings

...
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Service Status: ${if (isRunning) "Running" else "Stopped"}")
                    Switch(
                        checked = isRunning,
                        onCheckedChange = {
                            isRunning = it
                            if (it) {
                                val lastRpc = Prefs[Prefs.LAST_RUN_CUSTOM_RPC, ""]
                                if (lastRpc.isNotEmpty()) {
                                    val intent = Intent(context, CustomRpcService::class.java).apply {
                                        putExtra("RPC", lastRpc)
                                    }
                                    context.startService(intent)
                                }
                            } else {
                                context.stopService(Intent(context, CustomRpcService::class.java))
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = {
                        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                        intent?.apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            putExtra("NAVIGATE_TO", "custom_rpc")
                        }
                        context.startActivity(intent)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Configure RPC")
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Presets", style = MaterialTheme.typography.labelLarge)
                
                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp)
                ) {
                    items(configs) { config ->
                        ListItem(
                            headlineContent = { Text(config) },
                            modifier = Modifier.clickable {
                                val loadedConfig = ConfigUtils.loadConfig(context, config)
                                loadedConfig?.let {
                                    val json = ConfigUtils.run { it.dataToString() }
                                    Prefs[Prefs.LAST_RUN_CUSTOM_RPC] = json
                                    if (isRunning) {
                                        val intent = Intent(context, CustomRpcService::class.java).apply {
                                            putExtra("RPC", json)
                                        }
                                        context.startService(intent)
                                    }
                                }
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}

package com.my.axe.feature_overlay

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.my.axe.data.utils.ConfigUtils
import com.my.axe.feature_rpc_base.AppUtils
import com.my.axe.feature_rpc_base.services.CustomRpcService
import com.my.axe.preference.Prefs
import com.my.axe.feature_custom_rpc.UiEvent
import com.my.axe.feature_custom_rpc.UiState
import com.my.axe.feature_custom_rpc.components.CustomRpcForm
import com.my.axe.feature_custom_rpc.components.sheet.stringToData

@Composable
fun OverlayMenu(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isRunning by remember { mutableStateOf(AppUtils.customRpcRunning()) }
    val configs = remember { ConfigUtils.getAllConfigs(context) }
    
    var isEditMode by remember { mutableStateOf(false) }
    var currentUiState by remember { 
        val lastRpc = Prefs[Prefs.LAST_RUN_CUSTOM_RPC, ""]
        mutableStateOf(UiState(rpcConfig = lastRpc.stringToData())) 
    }

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
                        text = if (isEditMode) "Edit RPC" else "Axe Quick Controls",
                        style = MaterialTheme.typography.titleLarge
                    )
                    IconButton(onClick = { if (isEditMode) isEditMode = false else onDismiss() }) {
                        Icon(if (isEditMode) Icons.Default.ArrowBack else Icons.Default.Close, contentDescription = "Back/Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isEditMode) {
                    CustomRpcForm(
                        uiState = currentUiState,
                        onEvent = { event ->
                            if (event is UiEvent.SetFieldsFromConfig) {
                                currentUiState = currentUiState.copy(rpcConfig = event.rpc)
                                val json = ConfigUtils.run { event.rpc.dataToString() }
                                Prefs[Prefs.LAST_RUN_CUSTOM_RPC] = json
                                if (isRunning) {
                                    val intent = Intent(context, CustomRpcService::class.java).apply {
                                        putExtra("RPC", json)
                                    }
                                    context.startService(intent)
                                }
                            }
                        },
                        showEnableSwitch = false,
                        modifier = Modifier
                            .heightIn(max = 400.dp)
                            .verticalScroll(rememberScrollState())
                    )
                } else {
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
                                    val json = ConfigUtils.run { currentUiState.rpcConfig.dataToString() }
                                    if (json.isNotEmpty()) {
                                        val intent = Intent(context, CustomRpcService::class.java).apply {
                                            putExtra("RPC", json)
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
                        onClick = { isEditMode = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Configure RPC")
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Presets", style = MaterialTheme.typography.labelLarge)
                    
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 200.dp)
                    ) {
                        items(configs) { config ->
                            ListItem(
                                headlineContent = { Text(config) },
                                modifier = Modifier.clickable {
                                    val loadedConfig = ConfigUtils.loadConfig(context, config)
                                    loadedConfig?.let {
                                        currentUiState = currentUiState.copy(rpcConfig = it)
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
}

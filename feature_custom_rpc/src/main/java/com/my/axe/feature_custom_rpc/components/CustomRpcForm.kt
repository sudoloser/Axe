package com.my.axe.feature_custom_rpc.components

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.my.axe.data.rpc.Constants
import com.my.axe.data.rpc.Constants.MAX_ALLOWED_CHARACTER_LENGTH
import com.my.axe.data.utils.uriToFile
import com.my.axe.feature_custom_rpc.UiEvent
import com.my.axe.feature_custom_rpc.UiState
import com.my.axe.feature_rpc_base.AppUtils
import com.my.axe.feature_rpc_base.services.AppDetectionService
import com.my.axe.feature_rpc_base.services.CustomRpcService
import com.my.axe.feature_rpc_base.services.ExperimentalRpc
import com.my.axe.feature_rpc_base.services.MediaRpcService
import com.my.axe.preference.Prefs
import com.my.axe.resources.R
import com.my.axe.ui.components.RpcField
import com.my.axe.ui.components.SwitchBar
import com.my.axe.feature_custom_rpc.components.sheet.dataToString
import kotlinx.coroutines.launch

@Composable
fun CustomRpcForm(
    uiState: UiState,
    onEvent: (UiEvent) -> Unit,
    modifier: Modifier = Modifier,
    showEnableSwitch: Boolean = true
) {
    val context = LocalContext.current
    var isCustomRpcEnabled by remember {
        mutableStateOf(AppUtils.customRpcRunning())
    }
    val scope = rememberCoroutineScope()

    Column(modifier = modifier) {
        if (showEnableSwitch) {
            SwitchBar(
                title = stringResource(id = R.string.enable_customRpc),
                isChecked = isCustomRpcEnabled
            ) {
                isCustomRpcEnabled = !isCustomRpcEnabled
                when (isCustomRpcEnabled) {
                    true -> {
                        context.stopService(Intent(context, AppDetectionService::class.java))
                        context.stopService(Intent(context, MediaRpcService::class.java))
                        context.stopService(Intent(context, ExperimentalRpc::class.java))
                        val intent = Intent(context, CustomRpcService::class.java)
                        val string = uiState.rpcConfig.dataToString()
                        intent.putExtra("RPC", string)
                        Prefs[Prefs.LAST_RUN_CUSTOM_RPC] = string
                        context.startService(intent)
                    }
                    false -> context.stopService(Intent(context, CustomRpcService::class.java))
                }
            }
        }

        with(uiState.rpcConfig) {
            RpcField(value = name, label = R.string.activity_name) {
                onEvent(UiEvent.SetFieldsFromConfig(uiState.rpcConfig.copy(name = it)))
            }

            RpcField(value = details, label = R.string.activity_details) {
                onEvent(UiEvent.SetFieldsFromConfig(uiState.rpcConfig.copy(details = it)))
            }

            RpcField(value = state, label = R.string.activity_state) {
                onEvent(UiEvent.SetFieldsFromConfig(uiState.rpcConfig.copy(state = it)))
            }

            Row {
                RpcField(
                    value = partyCurrentSize,
                    label = R.string.party_current,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                ) {
                    onEvent(UiEvent.SetFieldsFromConfig(uiState.rpcConfig.copy(partyCurrentSize = it)))
                }
                AnimatedVisibility(visible = partyCurrentSize.isNotBlank()) {
                    RpcField(
                        value = partyMaxSize,
                        label = R.string.party_max,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    ) {
                        onEvent(UiEvent.SetFieldsFromConfig(uiState.rpcConfig.copy(partyMaxSize = it)))
                    }
                }
            }

            RpcField(
                value = timestampsStart,
                label = R.string.activity_start_timestamps,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.EditCalendar,
                        contentDescription = null,
                        modifier = Modifier.clickable {
                            onEvent(UiEvent.TriggerStartTimeStampsDialog)
                        })
                },
                content = {
                    if (uiState.showStartTimeStampsPickerDialog) {
                        DateTimePickerDialog(
                            selectedDate = timestampsStart.toLongOrNull(),
                            onDismiss = { onEvent(UiEvent.TriggerStartTimeStampsDialog) }
                        ) {
                            onEvent(UiEvent.SetFieldsFromConfig(uiState.rpcConfig.copy(timestampsStart = it.toString())))
                        }
                    }
                }) {
                onEvent(UiEvent.SetFieldsFromConfig(uiState.rpcConfig.copy(timestampsStart = it)))
            }

            // ... images, type, etc can be added here as needed for overlay
            // For now, let's keep the main fields to keep it compact
        }
    }
}

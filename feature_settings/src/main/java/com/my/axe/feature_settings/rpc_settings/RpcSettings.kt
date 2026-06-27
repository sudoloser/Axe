/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * RpcSettings.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.axe.feature_settings.rpc_settings

import android.os.Build
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DoNotDisturbOn
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.SmartButton
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.my.axe.data.rpc.Constants
import com.my.axe.domain.model.rpc.RpcButtons
import com.my.axe.preference.Prefs
import com.my.axe.resources.R
import com.my.axe.ui.components.BackButton
import com.my.axe.ui.components.RpcField
import com.my.axe.ui.components.SettingItem
import com.my.axe.ui.components.Subtitle
import com.my.axe.ui.components.dialog.SingleChoiceItem
import com.my.axe.ui.components.preference.PreferenceSwitch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

import androidx.compose.material.icons.filled.Bolt
import rikka.shizuku.Shizuku
import com.blankj.utilcode.util.AppUtils as AppUtilsCode

import android.content.pm.PackageManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RpcSettings(onBackPressed: () -> Boolean) {
    val context = LocalContext.current
    var useShizuku by remember { mutableStateOf(Prefs[Prefs.USE_SHIZUKU, false]) }
    var shizukuAvailable by remember { mutableStateOf(false) }
    var shizukuPermissionGranted by remember { mutableStateOf(false) }

    val permissionListener = remember {
        Shizuku.OnRequestPermissionResultListener { _, grantResult ->
            shizukuPermissionGranted = grantResult == PackageManager.PERMISSION_GRANTED
        }
    }

    DisposableEffect(Unit) {
        Shizuku.addRequestPermissionResultListener(permissionListener)
        onDispose {
            Shizuku.removeRequestPermissionResultListener(permissionListener)
        }
    }

    LaunchedEffect(Unit) {
        shizukuAvailable = Shizuku.pingBinder()
        if (shizukuAvailable) {
            shizukuPermissionGranted = Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        }
    }
    var isLowResIconsEnabled by remember { mutableStateOf(Prefs[Prefs.RPC_USE_LOW_RES_ICON, false]) }
    var useRemoteGateway by remember { mutableStateOf(Prefs[Prefs.USE_REMOTE_GATEWAY, false]) }
    var showRemoteGatewayDialog by remember { mutableStateOf(false) }
    var remoteGatewayConfirmationText by remember { mutableStateOf("") }
    
    var useImgur by remember { mutableStateOf(Prefs[Prefs.USE_IMGUR, false]) }
    var configsDir by remember { mutableStateOf(Prefs[Prefs.CONFIGS_DIRECTORY, ""]) }
    var showDirConfigDialog by remember { mutableStateOf(false) }
    var useButtonConfigs by remember { mutableStateOf(Prefs[Prefs.USE_RPC_BUTTONS, false]) }
    var showButtonsConfigDialog by remember { mutableStateOf(false) }
    var rpcButtons by remember {
        mutableStateOf(Json.decodeFromString<RpcButtons>(Prefs[Prefs.RPC_BUTTONS_DATA, "{}"]))
    }
    var customActivityStatus by remember {
        mutableStateOf(Prefs[Prefs.CUSTOM_ACTIVITY_STATUS, "dnd"])
    }
    var customActivityType by remember {
        mutableStateOf(Prefs[Prefs.CUSTOM_ACTIVITY_TYPE, 0].toString())
    }
    var showActivityTypeDialog by remember {
        mutableStateOf(false)
    }
    var showActivityStatusDialog by remember {
        mutableStateOf(false)
    }
    var showApplicationIdDialog by remember {
        mutableStateOf(false)
    }
    var showImgurClientIdDialog by remember {
        mutableStateOf(false)
    }
    var allowExternalApps by remember { mutableStateOf(Prefs[Prefs.ALLOW_EXTERNAL_APPS, true]) }
    var customApplicationId by remember { mutableStateOf(Prefs[Prefs.CUSTOM_ACTIVITY_APPLICATION_ID, ""]) }
    var imgurClientId by remember { mutableStateOf(Prefs[Prefs.IMGUR_CLIENT_ID, Constants.IMGUR_CLIENT_ID]) }

    Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
        TopAppBar(
            title = { Text(stringResource(id = R.string.settings)) },
            navigationIcon = { BackButton { onBackPressed() } }
        )
    }) { paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues)) {
            item {
                Subtitle(text = stringResource(R.string.general_settings))
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                item {
                    SettingItem(
                        title = stringResource(id = R.string.configs_directory),
                        description = configsDir.ifEmpty { stringResource(id = R.string.custom_rpc_directory) },
                        icon = Icons.Default.Storage,
                    ) {
                        showDirConfigDialog = true
                    }
                }
            }
            item {
                PreferenceSwitch(
                    title = stringResource(id = R.string.use_custom_buttons),
                    description = stringResource(id = R.string.use_custom_buttons_desc),
                    icon = Icons.Default.Tune,
                    isChecked = useButtonConfigs,
                ) {
                    useButtonConfigs = !useButtonConfigs
                    Prefs[Prefs.USE_RPC_BUTTONS] = useButtonConfigs
                }
            }
            item {
                AnimatedVisibility(visible = useButtonConfigs) {
                    SettingItem(
                        title = stringResource(R.string.rpc_buttons),
                        description = stringResource(id = R.string.rpc_settings_button_configs),
                        icon = Icons.Default.SmartButton
                    ) {
                        showButtonsConfigDialog = true
                    }
                }
            }
            item {
                SettingItem(
                    title = stringResource(id = R.string.custom_activity_type),
                    description = stringResource(id = R.string.custom_activity_type_desc),
                    icon = Icons.Default.Code
                ) {
                    showActivityTypeDialog = true
                }

            }
            item {
                SettingItem(
                    title = stringResource(id = R.string.custom_activity_status),
                    description = stringResource(id = R.string.custom_activity_status_desc),
                    icon = Icons.Default.DoNotDisturbOn
                ) {
                    showActivityStatusDialog = true
                }
            }
            item {
                Subtitle(text = stringResource(id = R.string.advance_settings))
            }
            item {
                PreferenceSwitch(
                    title = stringResource(id = R.string.remote_gateway),
                    description = stringResource(id = R.string.remote_gateway_desc),
                    icon = Icons.Default.Bolt,
                    isChecked = useRemoteGateway,
                ) {
                    if (!useRemoteGateway) {
                        showRemoteGatewayDialog = true
                    } else {
                       useRemoteGateway = false
                       Prefs[Prefs.USE_REMOTE_GATEWAY] = false
                       Toast.makeText(context, "Remote Gateway disabled. Restart RPC to apply.", Toast.LENGTH_SHORT).show()
                    }                }
            }
            item {
                AnimatedVisibility(visible = useRemoteGateway) {
                    Column {
                        var customUrl by remember { mutableStateOf(Prefs[Prefs.REMOTE_GATEWAY_URL, "https://axe-server.onrender.com/"]) }
                        var customSignature by remember { mutableStateOf(Prefs[Prefs.REMOTE_GATEWAY_SIGNATURE, ""]) }

                        RpcField(
                            value = customUrl,
                            label = R.string.remote_gateway_url,
                            onValueChange = {
                                customUrl = it
                                Prefs[Prefs.REMOTE_GATEWAY_URL] = it
                            }
                        )
                        RpcField(
                            value = customSignature,
                            label = R.string.remote_gateway_signature,
                            onValueChange = {
                                customSignature = it
                                Prefs[Prefs.REMOTE_GATEWAY_SIGNATURE] = it
                            }
                        )
                    }
                }
            }
            item {
                PreferenceSwitch(
                    title = stringResource(id = R.string.shizuku_detection),
                    description = when {
                        !shizukuAvailable -> stringResource(id = R.string.shizuku_not_running)
                        !shizukuPermissionGranted -> "Shizuku: Permission required"
                        else -> stringResource(id = R.string.shizuku_connected)
                    },
                    icon = Icons.Default.Bolt,
                    isChecked = useShizuku && shizukuAvailable && shizukuPermissionGranted,
                    enabled = shizukuAvailable
                ) {
                    if (shizukuAvailable) {
                        if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                            useShizuku = !useShizuku
                            Prefs[Prefs.USE_SHIZUKU] = useShizuku
                            shizukuPermissionGranted = true
                        } else {
                            Shizuku.requestPermission(101)
                        }
                    }
                }
            }
            if (!shizukuAvailable) {
                item {
                    SettingItem(
                        title = stringResource(id = R.string.shizuku_open),
                        description = stringResource(id = R.string.shizuku_not_running),
                        icon = Icons.Default.Bolt
                    ) {
                        try {
                            AppUtilsCode.launchApp("moe.shizuku.privileged.api")
                        } catch (e: Exception) {
                            Toast.makeText(context, "Shizuku not installed", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            item {
                PreferenceSwitch(
                    title = stringResource(id = R.string.use_imgur),
                    description = stringResource(id = R.string.use_imgur_desc),
                    icon = Icons.Default.Image,
                    isChecked = useImgur
                ) {
                    useImgur = !useImgur
                    Prefs[Prefs.USE_IMGUR] = useImgur
                }
            }
            item {
                AnimatedVisibility(visible = useImgur) {
                    SettingItem(
                        title = stringResource(id = R.string.set_imgur_client_id),
                        description = stringResource(id = R.string.set_imgur_client_id_desc),
                        icon = Icons.Default.Code
                    ) {
                        showImgurClientIdDialog = true
                    }
                }
            }
            item {
                PreferenceSwitch(
                    title = stringResource(id = R.string.use_low_res_icon),
                    description = stringResource(id = R.string.use_low_res_icon_desc),
                    icon = Icons.Default.HighQuality,
                    isChecked = isLowResIconsEnabled,
                ) {
                    isLowResIconsEnabled = !isLowResIconsEnabled
                    Prefs[Prefs.RPC_USE_LOW_RES_ICON] = isLowResIconsEnabled
                }
            }
            item {
                SettingItem(
                    title = stringResource(id = R.string.delete_saved_icon_urls),
                    description = stringResource(id = R.string.delete_saved_icon_urls_desc),
                    icon = Icons.Default.DeleteForever
                ) {
                    Prefs.remove(Prefs.SAVED_IMAGES)
                    Prefs.remove(Prefs.SAVED_ARTWORK)
                    Toast.makeText(context, "Done", Toast.LENGTH_SHORT).show()
                }
            }
        }
        if (showDirConfigDialog) {
            AlertDialog(
                onDismissRequest = { showDirConfigDialog = false },
                confirmButton = {},
                icon = {
                    Icon(
                        imageVector = Icons.Default.Storage, contentDescription = null
                    )
                },
                title = { Text(stringResource(R.string.select_directory)) },
                text = {
                    Column {
                        SingleChoiceItem(
                            text = Constants.APP_DIRECTORY,
                            selected = configsDir == Constants.APP_DIRECTORY
                        ) {
                            configsDir = Constants.APP_DIRECTORY
                            Prefs[Prefs.CONFIGS_DIRECTORY] = configsDir
                            showDirConfigDialog = false
                        }
                        SingleChoiceItem(
                            text = Constants.DOWNLOADS_DIRECTORY,
                            selected = configsDir == Constants.DOWNLOADS_DIRECTORY
                        ) {
                            configsDir = Constants.DOWNLOADS_DIRECTORY
                            Prefs[Prefs.CONFIGS_DIRECTORY] = configsDir
                            showDirConfigDialog = false
                        }
                    }
                })
        }
        if (showButtonsConfigDialog) {
            AlertDialog(
                properties = DialogProperties(usePlatformDefaultWidth = false),
                modifier = Modifier.padding(20.dp),
                onDismissRequest = { showButtonsConfigDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        Prefs[Prefs.RPC_BUTTONS_DATA] = Json.encodeToString(rpcButtons)
                        showButtonsConfigDialog = false
                    }
                    ) {
                        Text("Save")
                    }
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Storage, contentDescription = null
                    )
                },
                title = { Text(stringResource(R.string.enter_details)) },
                text = {
                    Column {
                        RpcField(
                            value = rpcButtons.button1,
                            label = R.string.activity_button1_text,
                            isError = rpcButtons.button1.length >= Constants.MAX_ALLOWED_CHARACTER_LENGTH,
                            errorMessage = stringResource(R.string.activity_button_max_character)
                        ) {
                            rpcButtons = rpcButtons.copy(button1 = it)
                        }
                        AnimatedVisibility(visible = rpcButtons.button1.isNotEmpty()) {
                            RpcField(
                                value = rpcButtons.button1Url,
                                label = R.string.activity_button1_url
                            ) {
                                rpcButtons = rpcButtons.copy(button1Url = it)
                            }
                        }
                        RpcField(
                            value = rpcButtons.button2,
                            label = R.string.activity_button2_text,
                            isError = rpcButtons.button2.length >= Constants.MAX_ALLOWED_CHARACTER_LENGTH,
                            errorMessage = stringResource(R.string.activity_button_max_character)
                        ) {
                            rpcButtons = rpcButtons.copy(button2 = it)
                        }
                        AnimatedVisibility(visible = rpcButtons.button2.isNotEmpty()) {
                            RpcField(
                                value = rpcButtons.button2Url,
                                label = R.string.activity_button2_url
                            ) {
                                rpcButtons = rpcButtons.copy(button2Url = it)
                            }
                        }
                    }
                })
        }

        if (showActivityTypeDialog) {
            AlertDialog(
                onDismissRequest = {
                    showActivityTypeDialog = false
                },
                text = {
                    var activityTypeisExpanded by remember {
                        mutableStateOf(false)
                    }
                    val icon =
                        if (activityTypeisExpanded) {
                            Icons.Default.KeyboardArrowUp
                        } else {
                            Icons.Default.KeyboardArrowDown
                        }
                    RpcField(
                        value = customActivityType,
                        label = R.string.activity_type,
                        readOnly = true,
                        onClick = {
                            activityTypeisExpanded = !activityTypeisExpanded
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = null
                            )
                        },
                        content = {
                            DropdownMenu(
                                expanded = activityTypeisExpanded, onDismissRequest = {
                                    activityTypeisExpanded = !activityTypeisExpanded
                                }, modifier = Modifier.fillMaxWidth()
                            ) {
                                Constants.ACTIVITY_TYPE.forEach { (label, value) ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(text = label)
                                        },
                                        onClick = {
                                            customActivityType = value.toString()
                                            activityTypeisExpanded = false
                                        },
                                    )
                                }
                            }
                        }
                    )

                },
                confirmButton = {
                    TextButton(onClick = {
                        if (customActivityType.toInt() in 0..5) {
                            Prefs[Prefs.CUSTOM_ACTIVITY_TYPE] = customActivityType.toInt()
                            showActivityTypeDialog = false
                        }
                    }) {
                        Text(text = stringResource(R.string.save))
                    }
                }
            )
        }

        if (showActivityStatusDialog) {
            AlertDialog(
                onDismissRequest = {
                    showActivityStatusDialog = false
                },
                confirmButton = {},
                text = {

                    Column {
                        Constants.ACTIVITY_STATUS.forEach { (resId, value) ->
                            SingleChoiceItem(
                                text = stringResource(resId),
                                selected = value == customActivityStatus
                            ) {
                                customActivityStatus = value
                                Prefs[Prefs.CUSTOM_ACTIVITY_STATUS] = value
                                showActivityStatusDialog = false
                            }
                        }
                    }
                }
            )
        }

        if (showApplicationIdDialog) {
            AlertDialog(
                onDismissRequest = {
                    showApplicationIdDialog = false
                },
                title = { Text("Application ID") },
                text = {
                    Column {
                        Spacer(modifier = Modifier.height(8.dp))
                        RpcField(
                            value = customApplicationId,
                            label = R.string.application_id,
                            isError = customApplicationId.length !in Constants.MAX_APPLICATION_ID_LENGTH_RANGE || !customApplicationId.all { it.isDigit() },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            onValueChange = { newText ->
                                if (newText.length <= Constants.MAX_APPLICATION_ID_LENGTH_RANGE.last && newText.all { it.isDigit() }) {
                                    customApplicationId = newText
                                }
                            }
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (customApplicationId.length !in Constants.MAX_APPLICATION_ID_LENGTH_RANGE || !customApplicationId.all { it.isDigit() }) {
                                Toast.makeText(
                                    context,
                                    "Please enter a valid Application ID",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Prefs[Prefs.CUSTOM_ACTIVITY_APPLICATION_ID] = customApplicationId
                                showApplicationIdDialog = false
                            }
                        }
                    ) {
                        Text(stringResource(R.string.save))
                    }
                },
            )
        }

        if (showImgurClientIdDialog) {
            AlertDialog(
                onDismissRequest = {
                    imgurClientId = Prefs[Prefs.IMGUR_CLIENT_ID, Constants.IMGUR_CLIENT_ID]
                    showImgurClientIdDialog = false
                },
                title = { Text(stringResource(id = R.string.set_imgur_client_id)) },
                text = {
                    Column {
                        Spacer(modifier = Modifier.height(8.dp))
                        RpcField(
                            value = imgurClientId,
                            label = R.string.imgur_client_id,
                            onValueChange = { newText ->
                                imgurClientId = newText
                            }
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (imgurClientId.isBlank()) {
                                imgurClientId = Constants.IMGUR_CLIENT_ID
                            }
                            Prefs[Prefs.IMGUR_CLIENT_ID] = imgurClientId
                            showImgurClientIdDialog = false
                        }
                    ) {
                        Text(stringResource(R.string.save))
                    }
                },
            )
        }

        if (showRemoteGatewayDialog) {
            AlertDialog(
                onDismissRequest = { 
                    showRemoteGatewayDialog = false
                    remoteGatewayConfirmationText = ""
                },
                title = { Text(stringResource(id = R.string.remote_gateway_warning_title)) },
                text = {
                    Column {
                        Text(stringResource(id = R.string.remote_gateway_warning_message))
                        Spacer(modifier = Modifier.height(16.dp))
                        RpcField(
                            value = remoteGatewayConfirmationText,
                            label = R.string.remote_gateway_confirm_placeholder,
                            onValueChange = { remoteGatewayConfirmationText = it }
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        enabled = remoteGatewayConfirmationText == stringResource(id = R.string.remote_gateway_confirm_placeholder),
                        onClick = {
                            useRemoteGateway = true
                            Prefs[Prefs.USE_REMOTE_GATEWAY] = true
                            showRemoteGatewayDialog = false
                            remoteGatewayConfirmationText = ""
                            Toast.makeText(context, "Remote Gateway enabled. Restart RPC to apply.", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text(stringResource(R.string.confirm))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { 
                        showRemoteGatewayDialog = false
                        remoteGatewayConfirmationText = ""
                    }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }
}

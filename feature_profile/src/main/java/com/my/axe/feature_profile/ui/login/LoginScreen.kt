/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * LoginScreen.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.axe.feature_profile.ui.login

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.my.axe.feature_profile.getUserInfo
import com.my.axe.feature_profile.ui.component.DiscordLoginButton
import com.my.axe.feature_profile.ui.component.DiscordLoginWebView
import com.my.axe.preference.Prefs
import com.my.axe.preference.Prefs.TOKEN
import com.my.axe.resources.R
import com.my.axe.ui.components.BackButton
import com.my.axe.ui.components.dialog.TextFieldDialog
import com.my.axe.ui.theme.DISCORD_GREY
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LoginScreen(
    onCompleted: () -> Unit,
    onBackPressed: () -> Unit
) {
    var buttonEnabledState by remember { mutableStateOf(true) }
    var uiState: LoginUiState by remember { mutableStateOf(LoginUiState.InitialState) }
    var showTokenDialog by remember { mutableStateOf(false) }
    var tokenValue by remember { mutableStateOf("") }
    
    val scope = rememberCoroutineScope()
    val modalBottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    if (showTokenDialog) {
        TextFieldDialog(
            visible = showTokenDialog,
            title = stringResource(R.string.login_with_token),
            description = stringResource(R.string.token_login_warning),
            value = tokenValue,
            onValueChange = { tokenValue = it },
            onDismissRequest = { showTokenDialog = false },
            onConfirm = {
                showTokenDialog = false
                Prefs[TOKEN] = it
                scope.launch {
                    uiState = LoginUiState.OnLoginCompleted
                    getUserInfo(it, onInfoSaved = {
                        onCompleted()
                    })
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        topBar = {
            TopAppBar(title = { },
                navigationIcon = { BackButton { onBackPressed() } })
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (uiState) {
                LoginUiState.InitialState -> {}
                LoginUiState.OnLoginClicked -> {
                    ModalBottomSheet(
                        onDismissRequest = {
                            uiState = LoginUiState.InitialState
                        },
                        sheetState = modalBottomSheetState,
                        dragHandle = {
                            BottomSheetDefaults.DragHandle()
                        },
                    ) {
                        DiscordLoginWebView {
                            Prefs[TOKEN] = it
                            scope.launch {
                                modalBottomSheetState.hide()
                                uiState = LoginUiState.OnLoginCompleted
                                getUserInfo(it, onInfoSaved = {
                                    onCompleted()
                                })
                            }
                        }
                    }
                }
                LoginUiState.OnLoginCompleted -> {
                    buttonEnabledState = false
                    CircularProgressIndicator()
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                DiscordLoginButton(
                    onClick = { uiState = LoginUiState.OnLoginClicked },
                    enabled = buttonEnabledState
                )
                if (buttonEnabledState) {
                    ElevatedButton(
                        onClick = { showTokenDialog = true },
                        modifier = Modifier.fillMaxWidth(0.8f),
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = DISCORD_GREY,
                            contentColor = Color.White.copy(alpha = 0.8f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_ticket),
                            tint = Color.Unspecified,
                            contentDescription = "token_login",
                            modifier = Modifier.padding(end = 5.dp)
                        )
                        Text(text = stringResource(R.string.login_with_token))
                    }
                    Text(
                        text = stringResource(R.string.passkey_login_not_working),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun LoginScreenPreview() {
    LoginScreen(
        onCompleted = {},
        onBackPressed = {}
    )
}
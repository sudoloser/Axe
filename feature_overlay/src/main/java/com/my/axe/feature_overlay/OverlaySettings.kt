package com.my.axe.feature_overlay

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.my.axe.data.utils.getInstalledApps
import com.my.axe.preference.Prefs
import com.my.axe.resources.R
import com.my.axe.ui.components.AppsItem
import com.my.axe.ui.components.BackButton
import com.my.axe.ui.components.SearchBar
import com.my.axe.ui.components.Subtitle
import com.my.axe.ui.components.preference.PreferenceSwitch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverlaySettings(
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    
    var useOverlay by remember { mutableStateOf(Prefs[Prefs.USE_OVERLAY, false]) }
    var systemWide by remember { mutableStateOf(Prefs[Prefs.OVERLAY_SYSTEM_WIDE, false]) }
    var opacity by remember { mutableStateOf(Prefs[Prefs.OVERLAY_OPACITY, 0.8f]) }
    var scale by remember { mutableStateOf(Prefs[Prefs.OVERLAY_SCALE, 1.0f]) }
    
    val hasOverlayPermission = remember { 
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Settings.canDrawOverlays(context) else true 
    }

    val apps = remember { 
        getInstalledApps(context, Prefs::isOverlayWhitelisted).sortedBy { !it.isChecked }
    }
    var whitelistedApps by remember { mutableStateOf(apps.associate { it.pkg to it.isChecked }) }
    var searchText by remember { mutableStateOf("") }
    var isSearchBarVisible by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.overlay_settings)) },
                navigationIcon = { BackButton { onBackPressed() } },
                actions = {
                    if (isSearchBarVisible) {
                        Column(
                            modifier = Modifier.padding(10.dp)
                        ) {
                            SearchBar(
                                text = searchText,
                                onTextChanged = { searchText = it },
                                onClose = { isSearchBarVisible = false },
                                placeholder = stringResource(id = R.string.search_placeholder)
                            )
                        }
                    } else {
                        IconButton(onClick = { isSearchBarVisible = !isSearchBarVisible }) {
                            Icon(imageVector = Icons.Outlined.Search, contentDescription = "Search")
                        }
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            item {
                Subtitle(text = stringResource(R.string.overlay_status))
            }
            item {
                PreferenceSwitch(
                    title = stringResource(R.string.overlay_enable),
                    description = if (hasOverlayPermission) stringResource(R.string.main_floatingOverlay_details) else stringResource(R.string.overlay_permission_required),
                    isChecked = useOverlay && hasOverlayPermission,
                    enabled = true
                ) {
                    if (!hasOverlayPermission) {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        )
                        context.startActivity(intent)
                    } else {
                        useOverlay = !useOverlay
                        Prefs[Prefs.USE_OVERLAY] = useOverlay
                        if (useOverlay) {
                            context.startService(Intent(context, OverlayService::class.java))
                        } else {
                            context.stopService(Intent(context, OverlayService::class.java))
                        }
                    }
                }
            }

            item {
                Subtitle(text = stringResource(R.string.overlay_appearance))
            }
            item {
                ListItem(
                    headlineContent = { Text("Opacity (${(opacity * 100).toInt()}%)") },
                    leadingContent = { Icon(Icons.Default.Opacity, contentDescription = null) },
                    supportingContent = {
                        Slider(
                            value = opacity,
                            onValueChange = { 
                                opacity = it
                                Prefs[Prefs.OVERLAY_OPACITY] = it
                            },
                            valueRange = 0.2f..1f
                        )
                    }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Scale (${(scale * 100).toInt()}%)") },
                    leadingContent = { Icon(Icons.Default.Scale, contentDescription = null) },
                    supportingContent = {
                        Slider(
                            value = scale,
                            onValueChange = { 
                                scale = it
                                Prefs[Prefs.OVERLAY_SCALE] = it
                            },
                            valueRange = 0.5f..1.5f
                        )
                    }
                )
            }
            
            item {
                Subtitle(text = stringResource(R.string.overlay_visibility))
            }
            item {
                PreferenceSwitch(
                    title = stringResource(R.string.overlay_system_wide),
                    description = stringResource(R.string.overlay_system_wide_desc),
                    isChecked = systemWide
                ) {
                    systemWide = !systemWide
                    Prefs[Prefs.OVERLAY_SYSTEM_WIDE] = systemWide
                }
            }
            
            if (!systemWide) {
                item {
                    Subtitle(text = stringResource(R.string.overlay_whitelist_apps))
                }
                items(apps.size, key = { idx -> apps[idx].pkg }) { idx ->
                    val app = apps[idx]
                    if (searchText.isEmpty() || app.name.contains(searchText, ignoreCase = true) || app.pkg.contains(searchText, ignoreCase = true)) {
                        AppsItem(
                            name = app.name,
                            pkg = app.pkg,
                            isChecked = whitelistedApps[app.pkg] ?: false,
                            onClick = {
                                Prefs.saveOverlayWhitelist(app.pkg)
                                whitelistedApps = whitelistedApps.toMutableMap().apply {
                                    this[app.pkg] = !this[app.pkg]!!
                                }
                            }
                        )
                    } else {
                        Spacer(modifier = Modifier.height(0.dp))
                    }
                }
            }
        }
    }
}

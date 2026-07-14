package com.my.axe.feature_settings

import android.app.StatusBarManager
import android.content.ComponentName
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.my.axe.domain.model.user.User
import com.my.axe.resources.R
import com.my.axe.ui.components.BackButton
import com.my.axe.ui.components.Subtitle
import com.my.axe.ui.components.chips

import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.foundation.clickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Sidebar(
    user: User?,
    showAxeQuickieRequestItem: Boolean,
    componentName: ComponentName,
    navigateToProfile: () -> Unit,
    navigateToSettings: () -> Unit,
    navigateToLanguages: () -> Unit = {},
    navigateToLogsScreen: () -> Unit = {},
    onReportBug: () -> Unit = {},
) {
    val uriHandler = LocalUriHandler.current
    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .width(260.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.padding(15.dp)
        ) {
            BadgedBox(
                badge = {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.offset(8.dp, (-8).dp)
                    ) {
                        Text(text = BuildConfig.VERSION_NAME)
                    }
                },
                modifier = Modifier.padding(top = 8.dp, bottom = 10.dp)
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleSmall
                )
            }
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier.weight(8f)
            ) {
                item {
                    SettingsItemCard(
                        title = stringResource(id = R.string.settings),
                        icon = Icons.Outlined.Settings
                    ) {
                        navigateToSettings()
                    }
                }
                item {
                    SettingsItemCard(
                        title = stringResource(id = R.string.language),
                        icon = Icons.Outlined.Language
                    ) {
                        navigateToLanguages()
                    }
                }
                item {
                    SettingsItemCard(
                        title = stringResource(id = R.string.logs),
                        icon = Icons.Outlined.BugReport
                    ) {
                        navigateToLogsScreen()
                    }
                }
                item {
                    HorizontalDivider()
                }
                item {
                    Subtitle(
                        text = stringResource(id = R.string.drawer_subtitle_help),
                        modifier = Modifier
                    )
                }
                /* TODO: Unhide when FAQ website is ready
                item {
                    SettingsItemCard(
                        title = stringResource(id = R.string.drawer_faq),
                        icon = Icons.AutoMirrored.Rounded.HelpOutline
                    ) {
                        uriHandler.openUri("https://kizzy.vercel.app/#FAQ")
                    }
                }
                */
                /* TODO: Make this visible again when the Axe discord is created
                item {
                    SettingsItemCard(
                        title = "Discord",
                        icon = ImageVector.vectorResource(id = R.drawable.ic_discord)
                    ) {
                        //Discord Server Link
                        uriHandler.openUri(chips.first().url)
                    }
                }
                */
                item {
                    SettingsItemCard(
                        title = stringResource(id = R.string.report_a_bug),
                        icon = Icons.Outlined.BugReport,
                    ) {
                        onReportBug()
                    }
                }
                item {
                    RequestQsTile(
                        visible = showAxeQuickieRequestItem,
                        componentName = componentName
                    )
                }
            }
            if (user != null) {
                ProfileCardSmall(user = user) { navigateToProfile() }
            }
        }
    }
}

@Composable
fun RequestQsTile(
    visible: Boolean = true,
    componentName: ComponentName,
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val ctx = LocalContext.current
    val label = stringResource(R.string.qs_tile_label)
    val statusBarManager: StatusBarManager = ctx.getSystemService(StatusBarManager::class.java)
    AnimatedVisibility(
        visible = visible,
        exit = fadeOut(tween(800))
    ) {
        SettingsItemCard(
            title = label,
            icon = Icons.Outlined.Star,
            selected = true,
        ) {
            statusBarManager.requestAddTileService(
                componentName,
                label,
                android.graphics.drawable.Icon.createWithResource(ctx, R.drawable.ic_tile_play),
                {},
            ) {}
        }
    }
}


@Composable
fun SettingsItemCard(
    title: String,
    icon: ImageVector,
    selected: Boolean = false,
    onClick: () -> Unit = {},
) {
    NavigationDrawerItem(
        modifier = Modifier.fillMaxWidth(),
        label = {
            Text(
                text = title,
                maxLines = 1,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp)
                    .copy(fontWeight = FontWeight.SemiBold),
            )
        },
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier
                    .padding(end = 5.dp)
                    .size(28.dp)
                )
        },
        selected = selected,
        onClick = { onClick() }
    )
}

@Composable
fun ProfileCardSmall(
    user: User?,
    navigateToProfile: () -> Unit,
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(15.dp)),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        onClick = navigateToProfile
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row {
                AsyncImage(
                    model = user?.getAvatarImage(),
                    modifier = Modifier
                        .padding(end = 10.dp)
                        .size(50.dp)
                        .clip(CircleShape),
                    contentDescription = user?.username,
                    error = painterResource(R.drawable.error_avatar),
                    placeholder = painterResource(R.drawable.error_avatar),
                )
                Text(
                    buildAnnotatedString {
                        withStyle(
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Left
                            ).toSpanStyle()
                        ) {
                            append((user?.globalName ?: user?.username) + "\r\n")
                        }
                        withStyle(
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                textAlign = TextAlign.Left
                            ).toSpanStyle()
                        ) {
                            append(user?.username)
                            if (user?.discriminator != "0")
                                append("#${user?.discriminator}")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun SettingsGroup(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            ),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            content = content
        )
    }
}

@Composable
fun SettingsRow(
    title: String,
    summary: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = summary,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackPressed: () -> Boolean,
    navigateToRpcSettings: () -> Unit,
    navigateToAppearance: () -> Unit,
    navigateToAbout: () -> Unit,
    navigateToDeveloperSettings: () -> Unit = {},
    navigateToLanguages: () -> Unit = {},
    navigateToLogsScreen: () -> Unit = {},
    isBeta: Boolean = false,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.settings)) },
                navigationIcon = { BackButton { onBackPressed() } }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            if (isBeta) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = androidx.compose.ui.Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Update,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = stringResource(id = R.string.latest_pre_release),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Update to the latest preview or beta features",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }

            item {
                SettingsGroup(title = "Personalization") {
                    SettingsRow(
                        title = stringResource(id = R.string.appearence),
                        summary = "Themes, dark mode preferences, and colors",
                        icon = Icons.Outlined.Palette,
                        onClick = navigateToAppearance
                    )
                }
            }

            item {
                SettingsGroup(title = "Integrations") {
                    SettingsRow(
                        title = stringResource(id = R.string.rpc_settings),
                        summary = "Discord integration, status, and button configs",
                        icon = Icons.Outlined.Settings,
                        onClick = navigateToRpcSettings
                    )
                }
            }

            item {
                SettingsGroup(title = "App Info & Support") {
                    SettingsRow(
                        title = stringResource(id = R.string.about),
                        summary = "Credits, version info, and developer profile",
                        icon = Icons.Outlined.Info,
                        onClick = navigateToAbout
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsRow(
                        title = stringResource(id = R.string.developer_settings),
                        summary = "Advanced debugging and log preferences",
                        icon = Icons.Outlined.Code,
                        onClick = navigateToDeveloperSettings
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun SidebarPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)
    ) {
        Sidebar(
            showAxeQuickieRequestItem = false,
            componentName = ComponentName("", ""),
            user = null,
            navigateToProfile = {},
            navigateToSettings = {},
            navigateToLanguages = {},
            navigateToLogsScreen = {}
        )
    }
}
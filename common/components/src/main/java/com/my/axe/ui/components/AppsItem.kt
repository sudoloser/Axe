/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * AppsItem.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.axe.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.blankj.utilcode.util.AppUtils
import com.my.axe.resources.R

import androidx.compose.foundation.combinedClickable

import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun AppsItem(
    name: String,
    pkg: String,
    isChecked: Boolean,
    customConfigs: List<String> = emptyList(),
    selectedConfig: String? = null,
    onConfigSelected: (String?) -> Unit = {},
    onPreviewClick: (String) -> Unit = {},
    onLongClick: () -> Unit = {},
    onClick: () -> Unit = {},
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.combinedClickable(
            onClick = { onClick() },
            onLongClick = { onLongClick() }
        )
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp) // Ensure bounded width for spatial environments
                    .padding(16.dp, 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val appIcon = remember(pkg) { AppUtils.getAppIcon(pkg) }
                AsyncImage(
                    model = appIcon,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    placeholder = painterResource(id = R.drawable.ic_apps),
                    error = painterResource(id = R.drawable.ic_apps),
                    modifier = Modifier
                        .size(70.dp)
                        .padding(10.dp),
                )

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = name,
                        maxLines = 1,
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                        color = MaterialTheme.colorScheme.onSurface,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (pkg.isNotEmpty())
                        Text(
                            text = pkg,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                }
                KSwitch(
                    checked = isChecked,
                    modifier = Modifier.padding(start = 20.dp, end = 6.dp),
                )
            }

            if (isChecked && customConfigs.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedConfig ?: stringResource(id = R.string.none_default),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(id = R.string.custom_rpc_config)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(id = R.string.none_default)) },
                                onClick = {
                                    onConfigSelected(null)
                                    expanded = false
                                }
                            )
                            customConfigs.forEach { config ->
                                DropdownMenuItem(
                                    text = { Text(config) },
                                    onClick = {
                                        onConfigSelected(config)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    if (selectedConfig != null) {
                        IconButton(
                            onClick = { onPreviewClick(selectedConfig) },
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = "Preview"
                            )
                        }
                    }
                }
            }
        }
    }
}
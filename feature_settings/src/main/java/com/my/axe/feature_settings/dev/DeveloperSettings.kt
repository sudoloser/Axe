package com.my.axe.feature_settings.dev

import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.my.axe.feature_settings.BuildConfig
import com.my.axe.preference.Prefs
import com.my.axe.resources.R
import com.my.axe.ui.components.BackButton
import com.my.axe.ui.components.SettingItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperSettings(
    onBackPressed: () -> Boolean,
) {
    val context = LocalContext.current
    val defaultApiUrl = try {
        com.my.axe.data.BuildConfig.BASE_URL
    } catch (_: Exception) {
        ""
    }
    var customApiUrl by remember {
        mutableStateOf(Prefs[Prefs.CUSTOM_API_BASE_URL, ""])
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.developer_settings)) },
                navigationIcon = { BackButton { onBackPressed() } }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                SettingItem(
                    title = stringResource(id = R.string.simulate_crash),
                    description = stringResource(id = R.string.simulate_crash_desc),
                    icon = Icons.Outlined.BugReport,
                ) {
                    Handler(Looper.getMainLooper()).post {
                        throw RuntimeException("Simulated crash triggered from Developer Settings")
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(id = R.string.api_base_url),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${stringResource(id = R.string.default)}: $defaultApiUrl",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = customApiUrl,
                    onValueChange = { customApiUrl = it },
                    label = { Text(stringResource(id = R.string.api_base_url)) },
                    placeholder = { Text(defaultApiUrl) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    singleLine = true,
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = {
                        Prefs[Prefs.CUSTOM_API_BASE_URL] = customApiUrl
                        Toast.makeText(context, stringResource(id = R.string.restart_to_apply), Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.padding(horizontal = 12.dp),
                ) {
                    Text(stringResource(id = R.string.save))
                }
                TextButton(
                    onClick = {
                        customApiUrl = ""
                        Prefs.remove(Prefs.CUSTOM_API_BASE_URL)
                        Toast.makeText(context, stringResource(id = R.string.restart_to_apply), Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.padding(horizontal = 12.dp),
                ) {
                    Text(stringResource(id = R.string.reset_to_default))
                }
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingItem(
                    title = stringResource(id = R.string.version),
                    description = BuildConfig.VERSION_NAME,
                    icon = Icons.Outlined.Info,
                )
            }
        }
    }
}

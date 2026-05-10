package com.my.axe.feature_rpc_base

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.my.axe.domain.model.rpc.RpcConfig
import com.my.axe.feature_rpc_base.Constants
import com.my.axe.feature_rpc_base.services.CustomRpcService
import com.my.axe.preference.Prefs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@AndroidEntryPoint
class AxeRpcReceiver : BroadcastReceiver() {

    @Inject
    lateinit var rateLimiter: RpcRateLimiter

    override fun onReceive(context: Context, intent: Intent) {
        // 1. Check if external apps are allowed
        val isAllowed = Prefs.get(Prefs.ALLOW_EXTERNAL_APPS, true)
        if (!isAllowed) {
            Log.w(TAG, "External apps are disabled in settings. Ignoring intent.")
            return
        }

        val action = intent.action ?: return
        val sourcePackage = intent.getStringExtra(AxeRpcIntents.EXTRA_SOURCE_PACKAGE) 
            ?: intent.getPackage() 
            ?: "unknown"

        when (action) {
            AxeRpcIntents.ACTION_UPDATE_PRESENCE -> {
                // 2. Rate limiting check
                if (!rateLimiter.canUpdate(sourcePackage)) {
                    Log.w(TAG, "Rate limit exceeded for $sourcePackage. Minimum 15s between updates.")
                    return
                }

                handleUpdatePresence(context, intent)
            }
            AxeRpcIntents.ACTION_CLEAR_PRESENCE -> {
                handleClearPresence(context)
            }
        }
    }

    private fun handleUpdatePresence(context: Context, intent: Intent) {
        try {
            val rpcConfig = RpcConfig(
                name = intent.getStringExtra(AxeRpcIntents.EXTRA_ACTIVITY_NAME) ?: "",
                details = intent.getStringExtra(AxeRpcIntents.EXTRA_DETAILS) ?: "",
                state = intent.getStringExtra(AxeRpcIntents.EXTRA_STATE) ?: "",
                largeImg = intent.getStringExtra(AxeRpcIntents.EXTRA_LARGE_IMAGE) ?: "",
                largeText = intent.getStringExtra(AxeRpcIntents.EXTRA_LARGE_TEXT) ?: "",
                smallImg = intent.getStringExtra(AxeRpcIntents.EXTRA_SMALL_IMAGE) ?: "",
                smallText = intent.getStringExtra(AxeRpcIntents.EXTRA_SMALL_TEXT) ?: "",
                button1 = intent.getStringExtra(AxeRpcIntents.EXTRA_BUTTON1_LABEL) ?: "",
                button1link = intent.getStringExtra(AxeRpcIntents.EXTRA_BUTTON1_URL) ?: "",
                button2 = intent.getStringExtra(AxeRpcIntents.EXTRA_BUTTON2_LABEL) ?: "",
                button2link = intent.getStringExtra(AxeRpcIntents.EXTRA_BUTTON2_URL) ?: "",
                timestampsStart = intent.getLongExtra(AxeRpcIntents.EXTRA_TIMESTAMP_START, 0L).let { if (it == 0L) "" else it.toString() },
                timestampsStop = intent.getLongExtra(AxeRpcIntents.EXTRA_TIMESTAMP_END, 0L).let { if (it == 0L) "" else it.toString() },
                type = intent.getStringExtra(AxeRpcIntents.EXTRA_ACTIVITY_TYPE)?.let { mapActivityType(it) } ?: "0",
                platform = intent.getStringExtra(AxeRpcIntents.EXTRA_APPLICATION_ID) ?: "" // Mapping app id to platform as a placeholder or use as is
            )

            val serviceIntent = Intent(context, CustomRpcService::class.java).apply {
                putExtra("RPC", Json.encodeToString(rpcConfig))
            }
            context.startService(serviceIntent)
            Log.d(TAG, "Presence update requested by intent")
        } catch (e: Exception) {
            Log.e(TAG, "Error handling presence update intent", e)
        }
    }

    private fun handleClearPresence(context: Context) {
        val serviceIntent = Intent(context, CustomRpcService::class.java).apply {
            action = Constants.ACTION_STOP_SERVICE
        }
        context.startService(serviceIntent)
        Log.d(TAG, "Presence clear requested by intent")
    }

    private fun mapActivityType(type: String): String {
        return when (type.lowercase()) {
            "playing" -> "0"
            "streaming" -> "1"
            "listening" -> "2"
            "watching" -> "3"
            "custom" -> "4"
            "competing" -> "5"
            else -> "0"
        }
    }

    companion object {
        private const val TAG = "AxeRpcReceiver"
    }
}

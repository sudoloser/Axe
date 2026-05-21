/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * Prefs.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.axe.preference

import com.my.axe.domain.model.release.Release
import com.my.axe.domain.model.user.User
import com.my.axe.preference.Prefs.isMediaAppEnabled
import com.my.axe.preference.Prefs.saveMediaAppToPrefs
import com.tencent.mmkv.MMKV
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.hours

object Prefs {
    val kv = MMKV.defaultMMKV()
    operator fun set(key: String, value: Any?) =
        when (value) {
            is String? -> kv.encode(key, value)
            is Int -> kv.encode(key, value)
            is Boolean -> kv.encode(key, value)
            is Float -> kv.encode(key, value)
            is Long -> kv.encode(key, value)
            else -> throw UnsupportedOperationException("Not yet implemented")
        }

    inline operator fun <reified T : Any> get(
        key: String,
        defaultValue: T? = null,
    ): T = when (T::class) {
        String::class -> kv.decodeString(key, defaultValue as String? ?: "") as T
        Int::class -> kv.decodeInt(key, defaultValue as? Int ?: -1) as T
        Boolean::class -> kv.decodeBool(key, defaultValue as? Boolean ?: false) as T
        Float::class -> kv.decodeFloat(key, defaultValue as? Float ?: -1f) as T
        Long::class -> kv.decodeLong(key, defaultValue as? Long ?: -1) as T
        else -> throw UnsupportedOperationException("Not yet implemented")
    }

    fun remove(key: String) {
        kv.removeValueForKey(key)
    }

    fun isAppEnabled(packageName: String?): Boolean {
        val apps = get(ENABLED_APPS, "[]")
        val enabledPackages: ArrayList<String> = Json.decodeFromString(apps)
        return enabledPackages.contains(packageName)
    }

    fun saveToPrefs(pkg: String) {
        val apps = get(ENABLED_APPS, "[]")
        val enabledPackages: ArrayList<String> = Json.decodeFromString(apps)
        if (enabledPackages.contains(pkg))
            enabledPackages.remove(pkg)
        else
            enabledPackages.add(pkg)

        set(ENABLED_APPS, Json.encodeToString(enabledPackages))
    }

    /**
     * Checks if the given app is enabled in the preferences for media rpc. This is used to filter
     * the media RPC based on the enabled media apps.
     *
     * @param packageName The package name of the app to check
     */
    fun isMediaAppEnabled(packageName: String?): Boolean {
        val apps = get(ENABLED_MEDIA_APPS, Json.encodeToString(predefinedMediaApps))
        val enabledPackages: ArrayList<String> = Json.decodeFromString(apps)
        return enabledPackages.contains(packageName)
    }

    /**
     * Saves the given package name to the preferences for media rpc. This is used to filter the
     * media RPC based on the enabled media apps.
     *
     * If the package name is already saved, it will be removed. If it is not saved, it will be
     * added.
     *
     * @param pkg The package name of the app to save
     */
    fun saveMediaAppToPrefs(pkg: String) {
        val apps = get(ENABLED_MEDIA_APPS, Json.encodeToString(predefinedMediaApps))
        val enabledPackages: ArrayList<String> = Json.decodeFromString(apps)
        if (enabledPackages.contains(pkg))
            enabledPackages.remove(pkg)
        else
            enabledPackages.add(pkg)

        set(ENABLED_MEDIA_APPS, Json.encodeToString(enabledPackages))
    }

    fun getUser(): User? {
        val userJson = get(USER_DATA, "")
        return when {
            userJson.isNotEmpty() -> Json.decodeFromString(userJson)
            else -> null
        }
    }

    fun getSavedLatestRelease(): Release? {
        val json = get(LATEST_RELEASE, "")
        return when {
            json.isNotEmpty() -> Json.decodeFromString(json)
            else -> null
        }
    }

    fun saveLatestRelease(release: Release) {
        set(LATEST_RELEASE, Json.encodeToString(release))
    }

    fun checkAndAutoDeleteSavedImages() {
        val lastDeleted = get(
            key = LAST_DELETED,
            // Force delete everyone's saved images for the first time
            defaultValue = System.currentTimeMillis() - 24.hours.inWholeMilliseconds
        )
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastDeleted > 24.hours.inWholeMilliseconds) {
            remove(SAVED_IMAGES)
            remove(SAVED_ARTWORK)
            set(LAST_DELETED, currentTime)
        }
    }

    //User Preferences
    const val USER_DATA = "user" //Json Data Referencing User_Data class
    const val TOKEN = "token"
    const val USER_ID = "user-id"
    const val USER_BIO = "user-bio"
    const val USER_NITRO = "user-nitro"
    const val LAST_RUN_CONSOLE_RPC = "last_run_console_rpc"
    const val LAST_RUN_CUSTOM_RPC = "last_run_custom_rpc"
    const val LANGUAGE = "language"
    const val ENABLED_APPS = "enabled_apps"
    const val ENABLED_MEDIA_APPS = "enabled_media_apps"
    const val ENABLED_EXPERIMENTAL_APPS = "enabled_experimental_apps"

    //Media Rpc Preferences
    const val MEDIA_RPC_ARTIST_NAME = "media_rpc_artist_name"
    const val MEDIA_RPC_ALBUM_NAME = "media_rpc_album_name"
    const val MEDIA_RPC_APP_ICON = "media_rpc_app_icon"
    const val MEDIA_RPC_ENABLE_TIMESTAMPS = "enable_timestamps"
    const val MEDIA_RPC_HIDE_ON_PAUSE = "hide_on_pause"
    const val MEDIA_RPC_SHOW_PLAYBACK_STATE = "show_playback_state"
    const val MEDIA_RPC_SHOW_SONG_AS_TITLE = "show_song_as_title"
    const val MEDIA_RPC_SHOW_ARTIST_AS_TITLE = "show_artist_as_title"

    //Rpc Setting Preferences
    const val USE_RPC_BUTTONS = "use_saved_rpc_buttons"
    const val RPC_BUTTONS_DATA = "saved_rpc_buttons_data"
    const val RPC_USE_LOW_RES_ICON = "use_low_res_app_icons"
    const val CONFIGS_DIRECTORY = "configs_directory"
    const val USE_IMGUR = "use_imgur"
    const val IMGUR_CLIENT_ID = "imgur_client_id"
    // Saved Image Asset ids
    const val SAVED_IMAGES = "saved_images"
    // Saved ArtWork
    const val SAVED_ARTWORK = "saved_artwork"

    //new
    const val DARK_THEME = "dark_theme_value"
    const val HIGH_CONTRAST = "high_contrast"
    const val DYNAMIC_COLOR = "dynamic_color"
    const val THEME_COLOR = "theme_color"
    const val CUSTOM_THEME_COLOR = "custom_theme_color"
    const val IS_FIRST_LAUNCHED = "is_first_launched"
    const val CUSTOM_ACTIVITY_TYPE = "custom_activity_type"
    const val SHOW_LOGS_IN_COMPACT_MODE = "logs_compact_mode"
    const val LOGS_AUTO_SCROLL = "logs_auto_scroll"
    const val ALLOW_EXTERNAL_APPS = "allow_external_apps"

    const val PALETTE_STYLE = "palette_style"

    const val CUSTOM_ACTIVITY_STATUS = "custom_activity_status"

    const val LATEST_RELEASE = "latest_release"

    // Last Deleted Time of Saved Images
    const val LAST_DELETED = "last_deleted"

    const val CUSTOM_ACTIVITY_APPLICATION_ID = "custom_activity_application_id_"

    /**
     * The list of media apps that are enabled by default. See [isMediaAppEnabled] and
     * [saveMediaAppToPrefs] for more information.
     */
    val predefinedMediaApps: List<String> = listOf(
        // music steaming apps
        "com.google.android.apps.youtube.music",
        "com.spotify.music",
        "com.google.android.music",
        "com.amazon.mp3",
        "com.apple.android.music",
        "com.soundcloud.android",
        "deezer.android.app",
        "com.jrtstudio.AnotherMusicPlayer",
        "com.pandora.android",
        "com.rhapsody",
        "com.sonyericsson.music",
        "com.aspiro.tidal",

        // music player apps
        "com.sec.android.app.music",
        "com.tbig.playerpro",

        // video streaming apps
        "com.google.android.apps.youtube.kids",
        "com.google.android.apps.youtube.unplugged",
        "com.google.android.youtube.googletv",
        "com.google.android.youtube.tv",
        "com.google.android.youtube",
        "com.netflix.mediaclient",
        "com.kick.mobile",
        "tv.twitch.android.app",

        // video player apps
        "com.mxtech.videoplayer.ad",
        "com.mxtech.videoplayer.pro",
        "com.google.android.apps.mediashell",
        "com.google.android.videos",
        "org.videolan.vlc",
    )

    //Experimental RPC Preferences
    const val EXPERIMENTAL_RPC_USE_APPS_RPC = "experimental_rpc_use_apps"
    const val EXPERIMENTAL_RPC_USE_MEDIA_RPC = "experimental_rpc_use_media"
    const val EXPERIMENTAL_RPC_TEMPLATE_NAME = "experimental_rpc_template_name"
    const val EXPERIMENTAL_RPC_TEMPLATE_DETAILS = "experimental_rpc_template_details"
    const val EXPERIMENTAL_RPC_TEMPLATE_STATE = "experimental_rpc_template_state"
    const val EXPERIMENTAL_RPC_TEMPLATE_ALBUM = "experimental_rpc_template_album"
    const val EXPERIMENTAL_RPC_APP_ACTIVITY_TYPES = "experimental_rpc_app_activity_types"
    const val EXPERIMENTAL_RPC_SHOW_COVER_ART = "experimental_rpc_show_cover_art"
    const val EXPERIMENTAL_RPC_SHOW_APP_ICON = "experimental_rpc_show_app_icon"
    const val EXPERIMENTAL_RPC_SHOW_PLAYBACK_STATE = "experimental_rpc_show_playback_state"
    const val EXPERIMENTAL_RPC_SHOW_ALBUM_TITLE = "experimental_rpc_show_album_title"
    const val EXPERIMENTAL_RPC_ENABLE_TIMESTAMPS = "experimental_rpc_enable_timestamps"
    const val EXPERIMENTAL_RPC_HIDE_ON_PAUSE = "experimental_rpc_hide_on_pause"
    const val USE_SHIZUKU = "use_shizuku"
    const val APP_CUSTOM_RPC_CONFIGS = "app_custom_rpc_configs"
    const val USE_OVERLAY = "use_overlay"
    const val OVERLAY_OPACITY = "overlay_opacity"
    const val OVERLAY_SCALE = "overlay_scale"
    const val OVERLAY_SYSTEM_WIDE = "overlay_system_wide"
    const val OVERLAY_WHITELIST = "overlay_whitelist"

    fun saveAppActivityType(packageName: String, activityType: Int) {
        val json = get(EXPERIMENTAL_RPC_APP_ACTIVITY_TYPES, "{}")
        val map: MutableMap<String, Int> = try {
            Json.decodeFromString(json)
        } catch (_: Exception) {
            mutableMapOf()
        }
        map[packageName] = activityType
        set(EXPERIMENTAL_RPC_APP_ACTIVITY_TYPES, Json.encodeToString(map))
    }

    fun getAppActivityTypes(): Map<String, Int> {
        val json = get(EXPERIMENTAL_RPC_APP_ACTIVITY_TYPES, "{}")
        return try {
            Json.decodeFromString(json)
        } catch (_: Exception) {
            emptyMap()
        }
    }

    fun saveAppCustomConfig(packageName: String, configName: String?) {
        val json = get(APP_CUSTOM_RPC_CONFIGS, "{}")
        val map: MutableMap<String, String> = try {
            Json.decodeFromString(json)
        } catch (_: Exception) {
            mutableMapOf()
        }
        if (configName == null) {
            map.remove(packageName)
        } else {
            map[packageName] = configName
        }
        set(APP_CUSTOM_RPC_CONFIGS, Json.encodeToString(map))
    }

    fun getAppCustomConfig(packageName: String): String? {
        val json = get(APP_CUSTOM_RPC_CONFIGS, "{}")
        return try {
            val map: Map<String, String> = Json.decodeFromString(json)
            map[packageName]
        } catch (_: Exception) {
            null
        }
    }

    fun getAppCustomConfigs(): Map<String, String> {
        val json = get(APP_CUSTOM_RPC_CONFIGS, "{}")
        return try {
            Json.decodeFromString(json)
        } catch (_: Exception) {
            emptyMap()
        }
    }

    fun saveOverlayWhitelist(packageName: String) {
        val apps = get(OVERLAY_WHITELIST, "[]")
        val whitelist: MutableSet<String> = try {
            Json.decodeFromString(apps)
        } catch (_: Exception) {
            mutableSetOf()
        }
        if (whitelist.contains(packageName))
            whitelist.remove(packageName)
        else
            whitelist.add(packageName)

        set(OVERLAY_WHITELIST, Json.encodeToString(whitelist))
    }

    fun isOverlayWhitelisted(packageName: String?): Boolean {
        if (packageName == null) return false
        val apps = get(OVERLAY_WHITELIST, "[]")
        val whitelist: Set<String> = try {
            Json.decodeFromString(apps)
        } catch (_: Exception) {
            emptySet()
        }
        return whitelist.contains(packageName)
    }

    fun isExperimentalAppEnabled(packageName: String?): Boolean {
        val apps = get(ENABLED_EXPERIMENTAL_APPS, "[]")
        val enabledPackages: ArrayList<String> = Json.decodeFromString(apps)
        return enabledPackages.contains(packageName)
    }

    fun saveExperimentalAppToPrefs(pkg: String) {
        val apps = get(ENABLED_EXPERIMENTAL_APPS, "[]")
        val enabledPackages: ArrayList<String> = Json.decodeFromString(apps)
        if (enabledPackages.contains(pkg))
            enabledPackages.remove(pkg)
        else
            enabledPackages.add(pkg)

        set(ENABLED_EXPERIMENTAL_APPS, Json.encodeToString(enabledPackages))
    }
}

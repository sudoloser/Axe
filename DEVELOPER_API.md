# Axe Public Intent API Documentation

## 1. Overview
The Axe Public Intent API allows third-party Android applications and games to update the user's Discord Rich Presence (RPC) status directly through the Axe app. By sending standardized Broadcast Intents, developers can display game activity, media playback, or custom status information on Discord without needing to implement the Discord Gateway protocol themselves.

## 2. Setup / Prerequisites

### Custom Permission
To interact with the Axe API, you must declare the Axe RPC permission in your app's `AndroidManifest.xml`:

```xml
<uses-permission android:name="com.my.axe.PERMISSION_RPC" />
```

### Package Visibility (Android 11+)
If your app targets Android 11 (API level 30) or higher, you should include Axe in your `<queries>` block:

```xml
<queries>
    <package android:name="com.my.axe" />
</queries>
```

## 3. Available Intent Actions

| Action | Description |
|:---|:---|
| `com.my.axe.UPDATE_PRESENCE` | Updates or starts a Discord presence with the provided details. |
| `com.my.axe.CLEAR_PRESENCE` | Stops the current Discord presence and clears the status. |

## 4. Supported Extras (UPDATE_PRESENCE)

All extras are passed via the `Intent` object. Most fields are optional.

| Extra Key | Type | Description |
|:---|:---|:---|
| `activity_name` | String | **Primary Title**. Usually the name of the app or game. |
| `details` | String | Secondary line of text (e.g., "Exploring the world"). |
| `state` | String | Third line of text (e.g., "Level 42", "In a Party"). |
| `large_image` | String | Public URL for the large image asset. |
| `large_text` | String | Hover text for the large image. |
| `small_image` | String | Public URL for the small image asset. |
| `small_text` | String | Hover text for the small image. |
| `button1_label` | String | Text label for the first interaction button. |
| `button1_url` | String | URL opened when Button 1 is clicked. |
| `button2_label` | String | Text label for the second interaction button. |
| `button2_url` | String | URL opened when Button 2 is clicked. |
| `timestamp_start` | Long | Epoch timestamp (ms) for the start of the activity. |
| `timestamp_end` | Long | Epoch timestamp (ms) for the end of the activity (shows "remaining"). |
| `application_id` | String | Custom Discord Application ID to use for this presence. |
| `activity_type` | String | One of: `playing`, `listening`, `watching`, `competing`, `streaming`. |
| `source_package` | String | Your app's package name (required for internal rate limiting). |

## 5. Complete Kotlin Example

```kotlin
import android.content.Context
import android.content.Intent

fun updateAxePresence(context: Context) {
    val intent = Intent("com.my.axe.UPDATE_PRESENCE").apply {
        `package` = "com.my.axe"
        
        // Basic Info
        putExtra("activity_name", "Kingdom of Kotlin")
        putExtra("details", "Battling the NullPointerException")
        putExtra("state", "Level 99 Mage")
        
        // Images (Direct public URLs)
        putExtra("large_image", "https://example.com/assets/world_map.png")
        putExtra("large_text", "The Great Plains")
        putExtra("small_image", "https://example.com/assets/mage_class.png")
        putExtra("small_text", "Rank: Grandmaster")
        
        // Timestamps
        putExtra("timestamp_start", System.currentTimeMillis())
        
        // Buttons
        putExtra("button1_label", "Join Party")
        putExtra("button1_url", "https://mygame.com/join/12345")
        
        // Activity Type
        putExtra("activity_type", "playing")
        
        // Mandatory for rate limiting
        putExtra("source_package", context.packageName)
    }
    
    context.sendBroadcast(intent)
}
```

## 6. Java Example

```java
import android.content.Context;
import android.content.Intent;

public void updateDiscordPresence(Context context) {
    Intent intent = new Intent("com.my.axe.UPDATE_PRESENCE");
    intent.setPackage("com.my.axe");

    intent.putExtra("activity_name", "Java Legends");
    intent.putExtra("details", "Managing GC Overhead");
    intent.putExtra("large_image", "https://example.com/java_logo.png");
    intent.putExtra("timestamp_start", System.currentTimeMillis());
    intent.putExtra("activity_type", "playing");
    intent.putExtra("source_package", context.getPackageName());

    context.sendBroadcast(intent);
}
```

## 7. Best Practices & Tips

- **Rate Limiting**: Axe enforces a minimum interval of **15 seconds** between updates from the same source app. Rapidly sending intents will result in them being ignored.
- **Image URLs**: Discord requires direct public URLs for images. Ensure your URLs are reachable and point directly to an image file (PNG/JPG/GIF).
- **User Preference**: Always check if the user has enabled the "Allow external apps" setting in Axe.
- **Background Context**: If sending intents from a background service or game loop, ensure you have the appropriate context.

## 8. Clear Presence Example

To clear the user's status when they exit your app or stop an activity:

```kotlin
val intent = Intent("com.my.axe.CLEAR_PRESENCE").apply {
    `package` = "com.my.axe"
}
context.sendBroadcast(intent)
```

## 9. Full Constants Reference

Use these keys exactly as defined below to ensure compatibility:

| Constant | Value |
|:---|:---|
| **Action: Update** | `com.my.axe.UPDATE_PRESENCE` |
| **Action: Clear** | `com.my.axe.CLEAR_PRESENCE` |
| **Extra: Activity Name** | `activity_name` |
| **Extra: Details** | `details` |
| **Extra: State** | `state` |
| **Extra: Large Image** | `large_image` |
| **Extra: Large Text** | `large_text` |
| **Extra: Small Image** | `small_image` |
| **Extra: Small Text** | `small_text` |
| **Extra: Button 1 Label** | `button1_label` |
| **Extra: Button 1 URL** | `button1_url` |
| **Extra: Button 2 Label** | `button2_label` |
| **Extra: Button 2 URL** | `button2_url` |
| **Extra: Time Start** | `timestamp_start` |
| **Extra: Time End** | `timestamp_end` |
| **Extra: App ID** | `application_id` |
| **Extra: Activity Type** | `activity_type` |
| **Extra: Source Pkg** | `source_package` |

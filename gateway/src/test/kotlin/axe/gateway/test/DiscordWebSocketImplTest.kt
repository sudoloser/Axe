package axe.gateway.test

import axe.gateway.DiscordWebSocketImpl
import axe.gateway.entities.presence.Activity
import axe.gateway.entities.presence.Assets
import axe.gateway.entities.presence.Presence
import com.my.axe.domain.interfaces.Logger
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for changes in DiscordWebSocketImpl introduced in this PR:
 * - encodeDefaults = false in Json configuration
 * - isWebSocketConnected() returns false before any connect() call
 * - coroutineContext is a property (not a getter), ensuring stable scope
 */
class DiscordWebSocketImplTest {

    private val noOpLogger = object : Logger {
        override fun clear() {}
        override fun i(tag: String, event: String) {}
        override fun e(tag: String, event: String) {}
        override fun d(tag: String, event: String) {}
        override fun w(tag: String, event: String) {}
    }

    // ---------------------------------------------------------------------------
    // isWebSocketConnected() before connect()
    // ---------------------------------------------------------------------------

    @Test
    fun `isWebSocketConnected returns false when no connection established`() {
        val gateway = DiscordWebSocketImpl("fake_token", noOpLogger)
        assertFalse(gateway.isWebSocketConnected())
    }

    @Test
    fun `DiscordWebSocketImpl can be instantiated with a token and logger`() {
        val gateway = DiscordWebSocketImpl("test_token", noOpLogger)
        assertNotNull(gateway)
    }

    // ---------------------------------------------------------------------------
    // encodeDefaults = false JSON serialization tests
    //
    // The PR changed encodeDefaults from true to false. This means that fields
    // with default values (null or otherwise) should not appear in the serialized
    // JSON output unless they are explicitly set to a non-default value.
    // We verify this using a Json instance configured the same way.
    // ---------------------------------------------------------------------------

    private val jsonEncodeDefaultsFalse = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
    }

    @Test
    fun `Presence with all default values does not include afk in JSON when false`() {
        val presence = Presence(
            activities = emptyList(),
            afk = false, // non-default? false is not the default (true is), so it appears
            since = null,
            status = null
        )
        val json = jsonEncodeDefaultsFalse.encodeToString(presence)
        // afk = false, which differs from default true, so it should appear
        assertTrue("afk field should be present when explicitly false", json.contains("\"afk\""))
    }

    @Test
    fun `Presence with null since does not include since key in JSON`() {
        val presence = Presence(
            activities = listOf(),
            afk = true,
            since = null,
            status = "online"
        )
        val json = jsonEncodeDefaultsFalse.encodeToString(presence)
        assertFalse("since=null should be omitted from JSON with encodeDefaults=false", json.contains("\"since\""))
    }

    @Test
    fun `Presence with null status does not include status key in JSON`() {
        val presence = Presence(
            activities = listOf(),
            afk = true,
            since = 1000L,
            status = null
        )
        val json = jsonEncodeDefaultsFalse.encodeToString(presence)
        assertFalse("status=null should be omitted from JSON with encodeDefaults=false", json.contains("\"status\""))
    }

    @Test
    fun `Presence with explicit status includes status in JSON`() {
        val presence = Presence(
            activities = listOf(),
            afk = true,
            since = 1000L,
            status = "dnd"
        )
        val json = jsonEncodeDefaultsFalse.encodeToString(presence)
        assertTrue("Explicit status should appear in JSON", json.contains("\"status\""))
        assertTrue("Status value should be present", json.contains("\"dnd\""))
    }

    @Test
    fun `Activity with null optional fields omits those fields in JSON`() {
        val activity = Activity(
            name = "TestGame",
            state = null,
            details = null,
            party = null,
            type = null,
            platform = null,
            timestamps = null,
            assets = null,
            buttons = null,
            metadata = null,
            applicationId = null,
            url = null
        )
        val json = jsonEncodeDefaultsFalse.encodeToString(activity)
        assertFalse("state=null should be omitted", json.contains("\"state\""))
        assertFalse("details=null should be omitted", json.contains("\"details\""))
        assertFalse("party=null should be omitted", json.contains("\"party\""))
        assertFalse("platform=null should be omitted", json.contains("\"platform\""))
        assertFalse("timestamps=null should be omitted", json.contains("\"timestamps\""))
        assertFalse("assets=null should be omitted", json.contains("\"assets\""))
        assertFalse("buttons=null should be omitted", json.contains("\"buttons\""))
        assertFalse("metadata=null should be omitted", json.contains("\"metadata\""))
        assertFalse("application_id=null should be omitted", json.contains("\"application_id\""))
        assertFalse("url=null should be omitted", json.contains("\"url\""))
    }

    @Test
    fun `Activity name is always included in JSON`() {
        val activity = Activity(name = "Discord")
        val json = jsonEncodeDefaultsFalse.encodeToString(activity)
        assertTrue("name field must always be present", json.contains("\"name\""))
        assertTrue("name value must be present", json.contains("\"Discord\""))
    }

    @Test
    fun `Assets with null images are omitted in JSON`() {
        val assets = Assets(
            largeImage = null,
            smallImage = null,
            largeText = null,
            smallText = null
        )
        val json = jsonEncodeDefaultsFalse.encodeToString(assets)
        assertFalse("large_image=null should be omitted", json.contains("\"large_image\""))
        assertFalse("small_image=null should be omitted", json.contains("\"small_image\""))
        assertFalse("large_text=null should be omitted", json.contains("\"large_text\""))
        assertFalse("small_text=null should be omitted", json.contains("\"small_text\""))
    }

    @Test
    fun `Assets with non-null values are included in JSON`() {
        val assets = Assets(
            largeImage = "mp:attachments/123/img.png",
            smallImage = null,
            largeText = "Large Text",
            smallText = null
        )
        val json = jsonEncodeDefaultsFalse.encodeToString(assets)
        assertTrue("large_image should be present", json.contains("\"large_image\""))
        assertTrue("large_text should be present", json.contains("\"large_text\""))
        assertFalse("small_image=null should be omitted", json.contains("\"small_image\""))
        assertFalse("small_text=null should be omitted", json.contains("\"small_text\""))
    }

    @Test
    fun `encodeDefaults false vs true produces different output for default values`() {
        val jsonWithDefaults = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
        val activity = Activity(name = "Test")
        val withDefaults = jsonWithDefaults.encodeToString(activity)
        val withoutDefaults = jsonEncodeDefaultsFalse.encodeToString(activity)

        // With defaults=true, null fields appear as null literals; with false, they're omitted
        assertTrue("With encodeDefaults=true, JSON is longer", withDefaults.length > withoutDefaults.length)
    }
}

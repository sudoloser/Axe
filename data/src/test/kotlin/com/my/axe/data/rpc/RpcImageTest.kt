package com.my.axe.data.rpc

import com.my.axe.domain.model.Contributor
import com.my.axe.domain.model.Game
import com.my.axe.domain.model.release.Release
import com.my.axe.domain.model.user.User
import com.my.axe.domain.repository.AxeRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.io.File

class RpcImageTest {

    // ---------------------------------------------------------------------------
    // Fake AxeRepository for testing ExternalImage caching
    // ---------------------------------------------------------------------------
    private class FakeRepository(
        private val imageResult: String? = "https://cdn.discordapp.com/external/resolved"
    ) : AxeRepository {
        var getImageCallCount = 0
        var lastRequestedUrl: String? = null

        override suspend fun getImage(url: String): String? {
            getImageCallCount++
            lastRequestedUrl = url
            return imageResult
        }

        override suspend fun uploadImage(file: File): String? = null
        override suspend fun getGames(): List<Game> = emptyList()
        override suspend fun getUser(userid: String): User =
            throw UnsupportedOperationException()
        override suspend fun getContributors(): List<Contributor> = emptyList()
        override suspend fun checkForUpdate(): Release =
            throw UnsupportedOperationException()
    }

    @Before
    fun setUp() {
        // Ensure cache is empty before each test
        RpcImage.clearCache()
    }

    @After
    fun tearDown() {
        // Clean up companion object state after each test
        RpcImage.clearCache()
    }

    // ---------------------------------------------------------------------------
    // DiscordImage.resolveImage() tests
    // ---------------------------------------------------------------------------

    @Test
    fun `DiscordImage without mp prefix gets prefix prepended`() = runBlocking {
        val repo = FakeRepository()
        val image = RpcImage.DiscordImage("attachments/1234567890/image.png")
        val result = image.resolveImage(repo)
        assertEquals("mp:attachments/1234567890/image.png", result)
    }

    @Test
    fun `DiscordImage already with mp prefix is returned unchanged`() = runBlocking {
        val repo = FakeRepository()
        val image = RpcImage.DiscordImage("mp:attachments/1234567890/image.png")
        val result = image.resolveImage(repo)
        assertEquals("mp:attachments/1234567890/image.png", result)
    }

    @Test
    fun `DiscordImage with empty string gets mp prefix`() = runBlocking {
        val repo = FakeRepository()
        val image = RpcImage.DiscordImage("")
        val result = image.resolveImage(repo)
        assertEquals("mp:", result)
    }

    @Test
    fun `DiscordImage with only mp prefix string returns mp prefix as-is`() = runBlocking {
        val repo = FakeRepository()
        val image = RpcImage.DiscordImage("mp:")
        val result = image.resolveImage(repo)
        assertEquals("mp:", result)
    }

    @Test
    fun `DiscordImage does not call repository`() = runBlocking {
        val repo = FakeRepository()
        val image = RpcImage.DiscordImage("attachments/abc/xyz.png")
        image.resolveImage(repo)
        assertEquals(0, repo.getImageCallCount)
    }

    // ---------------------------------------------------------------------------
    // ExternalImage.resolveImage() caching tests
    // ---------------------------------------------------------------------------

    @Test
    fun `ExternalImage calls repository on first resolve`() = runBlocking {
        val repo = FakeRepository(imageResult = "https://cdn.discordapp.com/external/abc")
        val image = RpcImage.ExternalImage("https://example.com/image.png")
        val result = image.resolveImage(repo)
        assertEquals("https://cdn.discordapp.com/external/abc", result)
        assertEquals(1, repo.getImageCallCount)
    }

    @Test
    fun `ExternalImage returns cached result on second resolve without calling repository again`() = runBlocking {
        val repo = FakeRepository(imageResult = "https://cdn.discordapp.com/external/cached")
        val url = "https://example.com/repeated.png"
        val image = RpcImage.ExternalImage(url)

        val first = image.resolveImage(repo)
        val second = image.resolveImage(repo)

        assertEquals(first, second)
        // Repository should only be called once due to caching
        assertEquals(1, repo.getImageCallCount)
    }

    @Test
    fun `ExternalImage cache is shared across instances with same URL`() = runBlocking {
        val repo = FakeRepository(imageResult = "https://cdn.discordapp.com/external/shared")
        val url = "https://example.com/shared.png"

        val image1 = RpcImage.ExternalImage(url)
        val image2 = RpcImage.ExternalImage(url)

        image1.resolveImage(repo)
        image2.resolveImage(repo)

        // Cache is companion-level, so second call from different instance also hits cache
        assertEquals(1, repo.getImageCallCount)
    }

    @Test
    fun `ExternalImage does not cache null repository result`() = runBlocking {
        val repo = FakeRepository(imageResult = null)
        val url = "https://example.com/missing.png"
        val image = RpcImage.ExternalImage(url)

        val first = image.resolveImage(repo)
        val second = image.resolveImage(repo)

        assertNull(first)
        assertNull(second)
        // Repository is called both times because null result is not cached
        assertEquals(2, repo.getImageCallCount)
    }

    @Test
    fun `ExternalImage returns correct URL from repository`() = runBlocking {
        val expectedUrl = "https://cdn.discordapp.com/external/xyz123"
        val repo = FakeRepository(imageResult = expectedUrl)
        val inputUrl = "https://myserver.com/avatar.png"
        val image = RpcImage.ExternalImage(inputUrl)

        val result = image.resolveImage(repo)

        assertEquals(expectedUrl, result)
        assertEquals(inputUrl, repo.lastRequestedUrl)
    }

    @Test
    fun `ExternalImage different URLs are cached independently`() = runBlocking {
        val repo = FakeRepository(imageResult = "https://cdn.discordapp.com/external/result")
        val url1 = "https://example.com/image1.png"
        val url2 = "https://example.com/image2.png"

        RpcImage.ExternalImage(url1).resolveImage(repo)
        RpcImage.ExternalImage(url1).resolveImage(repo) // should hit cache
        RpcImage.ExternalImage(url2).resolveImage(repo) // different URL, should call repo

        // url1 called once (cached on second call), url2 called once = 2 total
        assertEquals(2, repo.getImageCallCount)
    }

    // ---------------------------------------------------------------------------
    // clearCache() tests
    // ---------------------------------------------------------------------------

    @Test
    fun `clearCache forces ExternalImage to call repository again`() = runBlocking {
        val repo = FakeRepository(imageResult = "https://cdn.discordapp.com/external/clear")
        val url = "https://example.com/will-be-cleared.png"
        val image = RpcImage.ExternalImage(url)

        image.resolveImage(repo) // populates cache
        assertEquals(1, repo.getImageCallCount)

        RpcImage.clearCache()

        image.resolveImage(repo) // cache cleared, should call repo again
        assertEquals(2, repo.getImageCallCount)
    }

    @Test
    fun `clearCache does not affect DiscordImage resolution`() = runBlocking {
        val repo = FakeRepository()
        val image = RpcImage.DiscordImage("attachments/999/file.png")

        RpcImage.clearCache()
        val result = image.resolveImage(repo)

        assertEquals("mp:attachments/999/file.png", result)
    }

    @Test
    fun `clearCache on empty cache does not throw`() {
        // clearCache on already-empty cache should be a no-op
        RpcImage.clearCache()
        RpcImage.clearCache() // calling twice should be safe
    }
}
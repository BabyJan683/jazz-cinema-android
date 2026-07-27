package com.jazzcinema.app.network

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Resolves a Jazz Drive share URL to a direct streamable video URL.
 * Uses a two-step HTTP flow identical to the original JazzDriveResolver.java.
 * Always call from a background thread (Dispatchers.IO).
 */
object JazzDriveResolver {

    private const val BASE = "https://cloud.jazzdrive.com.pk"
    private const val UA   =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
        "AppleWebKit/537.36 Chrome/124.0.0.0 Safari/537.36"
    private val JSON_TYPE  = "application/json; charset=UTF-8".toMediaType()

    // In-memory cache: shareUrl → (playUrl, timestamp)
    private val cache      = mutableMapOf<String, Pair<String, Long>>()
    private const val TTL  = 3_600_000L   // 1 hour

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    /** @throws Exception on any resolution failure */
    fun resolve(shareUrl: String): String {
        // Return cached URL if still fresh
        cache[shareUrl]?.let { (url, ts) ->
            if (System.currentTimeMillis() - ts < TTL) return url
        }

        val token = extractToken(shareUrl)
            ?: throw Exception("Invalid Jazz Drive share URL")

        // ── Step 1: Login ─────────────────────────────────────────────────
        val loginBody = """{"data":{"accesstoken":"$token"}}"""
            .toRequestBody(JSON_TYPE)

        val loginReq = Request.Builder()
            .url("$BASE/sapi/link/login?action=login")
            .post(loginBody)
            .header("Accept",       "application/json,*/*")
            .header("Content-Type", "application/json;charset=UTF-8")
            .header("Origin",       BASE)
            .header("Referer",      "$BASE/share/f/$token")
            .header("User-Agent",   UA)
            .build()

        val loginResp = client.newCall(loginReq).execute()
        val loginText = loginResp.body?.string()
            ?: throw Exception("Empty login response")

        if (loginText.trimStart().startsWith("<")) {
            throw Exception("Jazz Drive blocked login (HTTP ${loginResp.code})")
        }

        val loginJson = JSONObject(loginText)
        val status    = loginJson.optString("status", "")
        if (status.isNotEmpty() && !status.equals("success", ignoreCase = true)) {
            val msg = loginJson.optString("message")
                .ifEmpty { loginJson.optString("msg", "Login failed") }
            throw Exception("JazzDrive: $msg")
        }

        val data     = loginJson.getJSONObject("data")
        val valKey   = data.getString("validationkey")
        val folderId = if (data.has("link") && !data.isNull("link")) {
            data.getJSONObject("link").getString("folderid")
        } else {
            data.getString("folderid")
        }
        if (folderId.isEmpty() || folderId == "-1") {
            throw Exception("JazzDrive: folderid missing")
        }

        // Extract JSESSIONID cookie from login response
        val sessionId = loginResp.headers("set-cookie")
            .firstOrNull { it.contains("JSESSIONID=") }
            ?.substringAfter("JSESSIONID=")
            ?.substringBefore(";")

        // ── Step 2: Fetch media list ───────────────────────────────────────
        val mediaBody =
            """{"data":{"fields":["name","url","playbackurl","videometadata"]}}"""
                .toRequestBody(JSON_TYPE)

        val mediaBuilder = Request.Builder()
            .url("$BASE/sapi/media?action=get&folderid=$folderId&shared=true&validationkey=$valKey")
            .post(mediaBody)
            .header("Accept",       "application/json,*/*")
            .header("Content-Type", "application/json;charset=UTF-8")
            .header("Origin",       BASE)
            .header("Referer",      "$BASE/share/f/$token")
            .header("User-Agent",   UA)

        if (!sessionId.isNullOrEmpty()) {
            mediaBuilder.header("Cookie", "JSESSIONID=$sessionId")
        }

        val mediaResp = client.newCall(mediaBuilder.build()).execute()
        val mediaText = mediaResp.body?.string()
            ?: throw Exception("Empty media response")

        if (mediaText.trimStart().startsWith("<")) {
            throw Exception("Jazz Drive rate-limited on media fetch")
        }

        val mediaJson  = JSONObject(mediaText)
        val mediaData  = mediaJson.getJSONObject("data")
        val items      = mediaData.getJSONArray("media")

        for (i in 0 until items.length()) {
            val item = items.getJSONObject(i)
            val url  = item.optString("url", "")
            val pb   = item.optString("playbackurl", "")

            val playUrl = when {
                url.startsWith("http") && url.contains("jazzdrive") -> url
                pb.startsWith("http")  && pb.contains("jazzdrive")  -> pb
                url.startsWith("http")                               -> url
                else                                                  -> continue
            }

            cache[shareUrl] = Pair(playUrl, System.currentTimeMillis())
            return playUrl
        }

        throw Exception("No playable URL found in Jazz Drive folder")
    }

    private fun extractToken(url: String): String? {
        val idx = url.indexOf("/share/f/")
        if (idx < 0) return null
        var t   = url.substring(idx + 9)
        val q   = t.indexOf('?');  if (q >= 0) t = t.substring(0, q)
        val s   = t.indexOf('/');  if (s >= 0) t = t.substring(0, s)
        return t.trim().takeIf { it.isNotEmpty() }
    }
}

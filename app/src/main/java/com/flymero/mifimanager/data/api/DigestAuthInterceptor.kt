package com.flymero.mifimanager.data.api

import okhttp3.Interceptor
import okhttp3.Response
import java.security.MessageDigest

class DigestAuthInterceptor(
    private var username: String = "",
    private var password: String = ""
) : Interceptor {

    private companion object {
        const val XML_ACTION_DIGEST_URI = "/cgi/xml_action.cgi"
    }

    private var realm: String = "Highwmg"
    private var nonce: String = ""
    private var qop: String = "auth"
    private var nonceCount: Int = 1

    @Synchronized
    fun updateCredentials(username: String, password: String) {
        this.username = username
        this.password = password
    }

    @Synchronized
    fun clearCredentials() {
        this.username = ""
        this.password = ""
        this.nonce = ""
        this.nonceCount = 1
    }

    @Synchronized
    fun updateNonce(realm: String, nonce: String, qop: String = "auth", nextNonceCount: Int = 1) {
        this.realm = realm
        this.nonce = nonce
        this.qop = qop
        this.nonceCount = nextNonceCount
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        if (!isRouterRequest(request.url.host)) return chain.proceed(request)
        if (username.isEmpty()) return chain.proceed(request)

        if (nonce.isNotEmpty()) {
            val authHeader = buildAuthHeader(request.method, digestUriFor(request.url.encodedPath))
            val authRequest = request.newBuilder()
                .header("Authorization", authHeader)
                .build()
            val response = chain.proceed(authRequest)
            if (response.code != 401) return response
            response.close()
        }

        // Try without auth first
        val initialResponse = chain.proceed(request)
        if (initialResponse.code != 401) return initialResponse

        val authHeaderStr = initialResponse.header("WWW-Authenticate")
        if (authHeaderStr == null || !authHeaderStr.startsWith("Digest")) return initialResponse

        val params = parseDigestHeader(authHeaderStr)
        updateNonce(
            realm = params["realm"] ?: realm,
            nonce = params["nonce"] ?: return initialResponse,
            qop = params["qop"] ?: "auth"
        )
        initialResponse.close()

        val authValue = buildAuthHeader(request.method, digestUriFor(request.url.encodedPath))
        val authenticatedRequest = request.newBuilder()
            .header("Authorization", authValue)
            .build()

        return chain.proceed(authenticatedRequest)
    }

    private fun isRouterRequest(host: String): Boolean =
        host == "192.168.1.1"

    private fun digestUriFor(path: String): String =
        if (path.endsWith("/xml_action.cgi")) XML_ACTION_DIGEST_URI else path

    private fun buildAuthHeader(method: String, uri: String): String {
        return synchronized(this) {
            val nc = nonceCount.toString(16).uppercase().padStart(8, '0')
            nonceCount += 1
            val cnonce = generateCnonce()
            val ha1 = md5("$username:$realm:$password")
            val ha2 = md5("$method:$uri")
            val response = md5("$ha1:$nonce:$nc:$cnonce:$qop:$ha2")
            "Digest username=\"$username\", realm=\"$realm\", " +
                "nonce=\"$nonce\", uri=\"$uri\", response=\"$response\", " +
                "qop=$qop, nc=$nc, cnonce=\"$cnonce\""
        }
    }

    private fun parseDigestHeader(header: String): Map<String, String> {
        val params = mutableMapOf<String, String>()
        val content = header.removePrefix("Digest").trim()
        val regex = Regex("""(\w+)="?([^",]+)"?""")
        regex.findAll(content).forEach { match ->
            params[match.groupValues[1]] = match.groupValues[2]
        }
        return params
    }

    private fun generateCnonce(): String {
        val bytes = ByteArray(16)
        java.security.SecureRandom().nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }.take(16)
    }

    private fun md5(input: String): String {
        val digest = MessageDigest.getInstance("MD5")
        val bytes = digest.digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}

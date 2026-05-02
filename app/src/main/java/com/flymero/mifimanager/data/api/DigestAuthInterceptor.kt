package com.flymero.mifimanager.data.api

import okhttp3.Interceptor
import okhttp3.Response
import java.security.MessageDigest

class DigestAuthInterceptor(
    private var username: String = "",
    private var password: String = ""
) : Interceptor {

    private var realm: String = "Highwmg"
    private var nonce: String = ""
    private var qop: String = "auth"

    fun updateCredentials(username: String, password: String) {
        this.username = username
        this.password = password
    }

    fun updateNonce(realm: String, nonce: String, qop: String = "auth") {
        this.realm = realm
        this.nonce = nonce
        this.qop = qop
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        if (username.isEmpty()) return chain.proceed(request)

        // For all requests, send Authorization header on first attempt if we have a nonce
        if (nonce.isNotEmpty()) {
            val authHeader = buildAuthHeader(request.method, request.url.encodedPath +
                if (request.url.encodedQuery != null) "?${request.url.encodedQuery}" else "")
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

        val authHeaderStr = initialResponse.header("WWW-Authenticate") ?: return initialResponse
        initialResponse.close()

        if (!authHeaderStr.startsWith("Digest")) return initialResponse

        val params = parseDigestHeader(authHeaderStr)
        realm = params["realm"] ?: realm
        nonce = params["nonce"] ?: return initialResponse
        qop = params["qop"] ?: "auth"

        val method = request.method
        val uri = request.url.encodedPath +
            if (request.url.encodedQuery != null) "?${request.url.encodedQuery}" else ""

        val authValue = buildAuthHeader(method, uri)
        val authenticatedRequest = request.newBuilder()
            .header("Authorization", authValue)
            .build()

        return chain.proceed(authenticatedRequest)
    }

    private fun buildAuthHeader(method: String, uri: String): String {
        val nc = "00000001"
        val cnonce = generateCnonce()
        val ha1 = md5("$username:$realm:$password")
        val ha2 = md5("$method:$uri")
        val response = md5("$ha1:$nonce:$nc:$cnonce:$qop:$ha2")
        return "Digest username=\"$username\", realm=\"$realm\", " +
            "nonce=\"$nonce\", uri=\"$uri\", response=\"$response\", " +
            "qop=$qop, nc=$nc, cnonce=\"$cnonce\""
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

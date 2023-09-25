package com.placetopay.p2pr.data.checkout.base

import android.os.Build
import java.math.BigInteger
import java.security.MessageDigest
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Base64
import java.util.Date
import java.util.Locale

data class CheckoutAuth(
    val login: String,
    val secretKey: String
) {
    private var nonce: String
    private var seed: String
    private var tranKey: String

    init {
        requireNotNull(login.isNotEmpty()) { "No login provided on authentication" }
        requireNotNull(secretKey.isNotEmpty()) { "No tranKey provided on authentication" }
        val nonceTemp = secureRandom(130)
        seed = currentDateInISO()
        tranKey = convertToBase64(convertToSHA256(nonceTemp + seed + secretKey, "SHA-256"))
        nonce = convertToBase64(nonceTemp.encodeToByteArray())
    }

    private fun secureRandom(bits: Int): String = BigInteger(bits, SecureRandom()).toString()

    private fun currentDateInISO(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val currentDateTime = LocalDateTime.now()
            val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
            currentDateTime.format(formatter)
        } else
            SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mmZ",
                Locale.getDefault()
            ).format(Date())
    }

    private fun convertToBase64(input: ByteArray): String {
        val encodedBytes: ByteArray = if (Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O
        ) {
            Base64.getEncoder().encode(input)
        } else {
            android.util.Base64.encode(input, android.util.Base64.NO_WRAP)
        }
        return String(encodedBytes)
    }

    private fun convertToSHA256(input: String, algorithm: String): ByteArray {
        val mDigest: MessageDigest = MessageDigest.getInstance(algorithm)
        return mDigest.digest(input.toByteArray())
    }
}
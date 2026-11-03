package com.sktech.wastetrack.util

object PhoneNormalizer {

    fun extractLast10(phone: String?): String {
        if (phone.isNullOrBlank()) return ""
        val digits = phone.filter { it.isDigit() }
        return if (digits.length >= 10) digits.takeLast(10) else digits
    }

    fun toStandardFormat(phone: String?): String {
        val last10 = extractLast10(phone)
        if (last10.length != 10) return phone?.trim().orEmpty()
        return "+91 ${last10.take(5)} ${last10.substring(5)}"
    }

    fun toE164(phone: String?): String {
        val last10 = extractLast10(phone)
        if (last10.length != 10) return phone?.trim().orEmpty()
        return "+91$last10"
    }

    fun toUserId(phone: String?): String {
        val last10 = extractLast10(phone)
        return if (last10.isNotBlank()) "user_$last10" else "user_default_001"
    }

    fun getPhoneVariants(phone: String?): List<String> {
        val last10 = extractLast10(phone)
        if (last10.isBlank()) return emptyList()
        val standard = toStandardFormat(phone)
        val e164 = toE164(phone)
        val raw = phone?.trim().orEmpty()
        return listOfNotNull(standard, e164, last10, "0$last10", if (raw.isNotBlank()) raw else null).distinct()
    }
}

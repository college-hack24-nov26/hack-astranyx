package com.sktech.wastetrack.domain.model.invoice

object EWayBillUtils {

    fun calculateValidityHours(distanceKm: Int): Int {
        val days = ((distanceKm + 199) / 200).coerceAtLeast(1)
        return days * 24
    }

    fun generateEWayBillNumber(): String {
        val prefix = "27"
        val datePart = String.format("%04d", (System.currentTimeMillis() % 10000))
        val randomPart = String.format("%06d", (100000..999999).random())
        return "$prefix$datePart$randomPart"
    }

    fun generateInvoiceNumber(): String {
        val seq = (1000..9999).random()
        return "INV/MH-26/$seq"
    }

    fun numberToWords(amount: Double): String {
        val units = arrayOf(
            "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten",
            "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"
        )
        val tens = arrayOf("", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety")

        fun convertLessThanOneThousand(n: Int): String {
            var current = ""
            var num = n
            if (num >= 100) {
                current += units[num / 100] + " Hundred "
                num %= 100
            }
            if (num >= 20) {
                current += tens[num / 10] + " "
                num %= 10
            }
            if (num > 0) {
                current += units[num] + " "
            }
            return current.trim()
        }

        val wholePart = amount.toLong()
        if (wholePart == 0L) return "Zero Rupees Only"

        var num = wholePart
        var result = ""

        val crore = num / 10000000
        num %= 10000000
        if (crore > 0) {
            result += "${convertLessThanOneThousand(crore.toInt())} Crore "
        }

        val lakh = num / 100000
        num %= 100000
        if (lakh > 0) {
            result += "${convertLessThanOneThousand(lakh.toInt())} Lakh "
        }

        val thousand = num / 1000
        num %= 1000
        if (thousand > 0) {
            result += "${convertLessThanOneThousand(thousand.toInt())} Thousand "
        }

        if (num > 0) {
            result += convertLessThanOneThousand(num.toInt())
        }

        return "INR ${result.trim()} Only"
    }
}


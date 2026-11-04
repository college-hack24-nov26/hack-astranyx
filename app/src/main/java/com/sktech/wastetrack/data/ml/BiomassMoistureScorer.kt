package com.sktech.wastetrack.data.ml

import android.graphics.Bitmap
import android.graphics.Color
import com.sktech.wastetrack.domain.model.BiomassDegradationScore
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.roundToInt

@Singleton
class BiomassMoistureScorer @Inject constructor() {

    fun calculateDegradation(bitmap: Bitmap): BiomassDegradationScore {
        val sampleDim = 120
        val scaled = Bitmap.createScaledBitmap(bitmap, sampleDim, sampleDim, false)

        var totalPixels = 0
        var greenCount = 0
        var goldenCuredCount = 0
        var brownWeatheredCount = 0
        var darkRotCount = 0
        var syntheticPlasticCount = 0
        var stoneDustCount = 0

        var sumR = 0L
        var sumG = 0L
        var sumB = 0L

        var gradientAccumulator = 0.0

        val pixelArray = IntArray(sampleDim * sampleDim)
        scaled.getPixels(pixelArray, 0, sampleDim, 0, 0, sampleDim, sampleDim)

        for (y in 0 until sampleDim) {
            for (x in 0 until sampleDim) {
                val idx = y * sampleDim + x
                val pixel = pixelArray[idx]

                val r = (pixel shr 16 and 0xFF)
                val g = (pixel shr 8 and 0xFF)
                val b = (pixel and 0xFF)

                sumR += r
                sumG += g
                sumB += b
                totalPixels++

                if (x < sampleDim - 1) {
                    val nextPixel = pixelArray[idx + 1]
                    val nextLum = ((nextPixel shr 16 and 0xFF) * 299 + (nextPixel shr 8 and 0xFF) * 587 + (nextPixel and 0xFF) * 114) / 1000
                    val curLum = (r * 299 + g * 587 + b * 114) / 1000
                    gradientAccumulator += abs(nextLum - curLum)
                }

                val hsv = FloatArray(3)
                Color.RGBToHSV(r, g, b, hsv)
                val hue = hsv[0]
                val sat = hsv[1]
                val value = hsv[2]

                if (hue in 65f..160f && sat > 0.22f && value > 0.20f) {
                    greenCount++
                } else if (hue in 28f..60f && sat in 0.25f..0.95f && value > 0.35f) {
                    goldenCuredCount++
                } else if (hue in 15f..27f && sat > 0.20f && value in 0.25f..0.75f) {
                    brownWeatheredCount++
                } else if (value < 0.20f) {
                    darkRotCount++
                } else if ((hue in 180f..330f && sat > 0.45f && value > 0.30f) || (hue in 345f..360f && sat > 0.70f && value > 0.60f)) {
                    syntheticPlasticCount++
                } else if (sat < 0.10f && value in 0.30f..0.85f) {
                    stoneDustCount++
                }
            }
        }

        if (scaled != bitmap) {
            scaled.recycle()
        }

        val greenRatio = greenCount.toFloat() / totalPixels
        val goldenRatio = goldenCuredCount.toFloat() / totalPixels
        val brownRatio = brownWeatheredCount.toFloat() / totalPixels
        val darkRatio = darkRotCount.toFloat() / totalPixels
        val plasticRatio = syntheticPlasticCount.toFloat() / totalPixels
        val stoneRatio = stoneDustCount.toFloat() / totalPixels

        val avgGradient = gradientAccumulator / (sampleDim * (sampleDim - 1))
        val textureScore = (avgGradient / 32.0).coerceIn(0.1, 1.0).toFloat()

        val rawMoisture = when {
            greenRatio > 0.45f -> 52f + (greenRatio * 18f)
            darkRatio > 0.35f -> 42f + (darkRatio * 22f)
            goldenRatio > 0.50f -> 10.5f + ((1f - goldenRatio) * 8f)
            goldenRatio > 0.30f -> 14.0f + ((1f - goldenRatio) * 12f) + (greenRatio * 15f)
            brownRatio > 0.40f -> 16.5f + (darkRatio * 15f)
            else -> 20.0f + (greenRatio * 25f) + (darkRatio * 18f)
        }.coerceIn(9.0f, 75.0f)

        val roundedMoisture = (rawMoisture * 10f).roundToInt() / 10f
        val dryMatter = (100.0f - roundedMoisture).coerceIn(25.0f, 91.0f)

        val hasPlastic = plasticRatio > 0.035f
        val hasStones = stoneRatio > 0.12f
        val hasSevereRot = darkRatio > 0.30f

        val contaminationNotice = when {
            hasPlastic -> "Synthetic plastic twine or film detected in biomass matrix"
            hasStones -> "Inert stone and soil dust content exceeds industrial limit"
            hasSevereRot -> "Fungal mold and waterlogged anaerobic decay detected"
            else -> null
        }

        val rotRiskPercent = ((darkRatio * 100f) + (if (roundedMoisture > 30f) (roundedMoisture - 30f) * 0.8f else 0f)).toInt().coerceIn(2, 98)

        var qualityScore = 80
        if (goldenRatio > 0.45f) qualityScore += 12
        if (textureScore > 0.65f) qualityScore += 6
        if (greenRatio > 0.35f) qualityScore -= 18
        if (darkRatio > 0.15f) qualityScore -= (darkRatio * 45).toInt()
        if (plasticRatio > 0.02f) qualityScore -= (plasticRatio * 150).toInt()
        if (stoneRatio > 0.08f) qualityScore -= (stoneRatio * 50).toInt()
        qualityScore = qualityScore.coerceIn(15, 98)

        val grade = when {
            qualityScore >= 85 && roundedMoisture <= 15.0f && contaminationNotice == null -> "Grade A (Prime Dry Cured Biomass)"
            qualityScore >= 65 && roundedMoisture <= 24.0f && !hasPlastic -> "Grade B (Standard Commercial Feedstock)"
            else -> "Grade C (Sub-Standard / High Moisture Spoilage)"
        }

        val degradationStatus = when {
            roundedMoisture <= 15f && darkRatio < 0.08f -> "Optimal Dry Matter (Prime Preservation)"
            roundedMoisture in 15.1f..22f -> "Safe Ambient Storage (Low Spoilage Velocity)"
            roundedMoisture in 22.1f..32f -> "Elevated Moisture (Active Respiration Zone)"
            else -> "Critical Degradation (Waterlogged / Microbial Decay)"
        }

        val avgR = (sumR / totalPixels).toInt().coerceIn(0, 255)
        val avgG = (sumG / totalPixels).toInt().coerceIn(0, 255)
        val avgB = (sumB / totalPixels).toInt().coerceIn(0, 255)
        val dominantHex = String.format("#%02X%02X%02X", avgR, avgG, avgB)

        return BiomassDegradationScore(
            estimatedMoisturePercent = roundedMoisture,
            biomassDryMatterPercent = (dryMatter * 10f).roundToInt() / 10f,
            qualityGrade = grade,
            qualityScore = qualityScore,
            degradationStatus = degradationStatus,
            rotRiskPercentage = rotRiskPercent,
            fibrousDensityScore = (textureScore * 100f).roundToInt() / 100f,
            dominantColorHex = dominantHex,
            contaminationNotice = contaminationNotice
        )
    }
}

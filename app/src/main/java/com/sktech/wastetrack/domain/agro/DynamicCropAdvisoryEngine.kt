package com.sktech.wastetrack.domain.agro

import com.sktech.wastetrack.domain.agro.model.CropSuitabilitySummary
import com.sktech.wastetrack.domain.model.Farm
import com.sktech.wastetrack.domain.model.WeatherData
import java.util.Calendar

enum class AdvisoryPriceTrend {
    RISING, STABLE, VOLATILE, FALLING
}

object DynamicCropAdvisoryEngine {

    enum class AgriSeason(val displayName: String, val months: Set<Int>) {
        KHARIF("Kharif (Monsoon)", setOf(Calendar.JUNE, Calendar.JULY, Calendar.AUGUST, Calendar.SEPTEMBER, Calendar.OCTOBER)),
        RABI("Rabi (Winter)", setOf(Calendar.OCTOBER, Calendar.NOVEMBER, Calendar.DECEMBER, Calendar.JANUARY, Calendar.FEBRUARY, Calendar.MARCH)),
        ZAID("Zaid (Summer)", setOf(Calendar.FEBRUARY, Calendar.MARCH, Calendar.APRIL, Calendar.MAY, Calendar.JUNE))
    }

    fun getCurrentAgriSeason(calendar: Calendar = Calendar.getInstance()): AgriSeason {
        val month = calendar.get(Calendar.MONTH)
        return when (month) {
            in setOf(Calendar.JUNE, Calendar.JULY, Calendar.AUGUST, Calendar.SEPTEMBER) -> AgriSeason.KHARIF
            in setOf(Calendar.OCTOBER, Calendar.NOVEMBER, Calendar.DECEMBER, Calendar.JANUARY, Calendar.FEBRUARY) -> AgriSeason.RABI
            else -> AgriSeason.ZAID
        }
    }

    data class CropSpecification(
        val cropName: String,
        val categoryName: String,
        val season: AgriSeason,
        val preferredSoilTypes: Set<String>,
        val minPh: Double,
        val maxPh: Double,
        val minTemp: Double,
        val maxTemp: Double,
        val waterNeed: String,
        val expectedYield: String,
        val residueBiomassPerAcreTons: Double,
        val residueMarketPricePerTon: Int,
        val residueName: String,
        val agronomicRationale: String,
        val keyAdvantages: List<String>,
        val commonRisks: List<String>
    )

    private val cropCatalog = listOf(
        CropSpecification(
            cropName = "Wheat (Lokwan / Sharbati / GW 322)",
            categoryName = "Grains",
            season = AgriSeason.RABI,
            preferredSoilTypes = setOf("ALLUVIAL", "BLACK_COTTON", "CLAY_LOAM"),
            minPh = 6.2, maxPh = 7.8,
            minTemp = 12.0, maxTemp = 30.0,
            waterNeed = "Medium (3-4 Irrigations)",
            expectedYield = "18-24 Quintals / Acre",
            residueBiomassPerAcreTons = 2.2,
            residueMarketPricePerTon = 5500,
            residueName = "Dry Cattle Straw (Bhusa)",
            agronomicRationale = "Top winter staple with high market liquidity across regional APMCs and high fodder straw valuation.",
            keyAdvantages = listOf(
                "High grain market liquidity with steady MSP procurement",
                "Cattle straw (Bhusa) sells at premium rates to local dairy co-operatives",
                "Fits smoothly into double-cropping following soybean or cotton"
            ),
            commonRisks = listOf(
                "Terminal heat stress if sowing delayed past mid-November",
                "Vulnerable to yellow rust under extended fog"
            )
        ),
        CropSpecification(
            cropName = "Chickpea / Bengal Gram (Chana - Digvijay)",
            categoryName = "Pulses",
            season = AgriSeason.RABI,
            preferredSoilTypes = setOf("BLACK_COTTON", "CLAY_LOAM", "ALLUVIAL"),
            minPh = 6.5, maxPh = 8.0,
            minTemp = 10.0, maxTemp = 28.0,
            waterNeed = "Low (1-2 Light Irrigations)",
            expectedYield = "9-13 Quintals / Acre",
            residueBiomassPerAcreTons = 1.1,
            residueMarketPricePerTon = 4200,
            residueName = "Gram Husk & Fodder Chaff",
            agronomicRationale = "Superior nitrogen-fixing pulse that restores exhausted soil after heavy cotton or sugarcane harvesting.",
            keyAdvantages = listOf(
                "Fixes 35-45 kg atmospheric Nitrogen per acre directly into soil",
                "Deep root structure conserves moisture and enhances subsoil aeration",
                "High protein husk valued by livestock and biomass aggregators"
            ),
            commonRisks = listOf(
                "Pod borer (Helicoverpa armigera) during pod development",
                "Root rot in waterlogged low-lying soil patches"
            )
        ),
        CropSpecification(
            cropName = "Mustard (Pusa Bold / Varuna)",
            categoryName = "Oilseeds",
            season = AgriSeason.RABI,
            preferredSoilTypes = setOf("SANDY_LOAM", "ALLUVIAL", "LOAM"),
            minPh = 6.0, maxPh = 7.5,
            minTemp = 10.0, maxTemp = 26.0,
            waterNeed = "Low (Residual Moisture)",
            expectedYield = "8-12 Quintals / Acre",
            residueBiomassPerAcreTons = 1.4,
            residueMarketPricePerTon = 2600,
            residueName = "Stalks for Biomass Briquettes",
            agronomicRationale = "Cold-season oilseed with low irrigation requirement and strong mandi pricing.",
            keyAdvantages = listOf(
                "Thrives on residual soil moisture post Kharif harvest",
                "Low input expenditure with minimal chemical fertilizer requirement",
                "High oilseed value and ready demand from oilseed processors"
            ),
            commonRisks = listOf(
                "Aphid infestation during flowering and pod development",
                "Frost susceptibility if temperature drops below 5 degrees C"
            )
        ),
        CropSpecification(
            cropName = "Onion (Nashik Red / AgriFound Light Red)",
            categoryName = "Vegetables",
            season = AgriSeason.RABI,
            preferredSoilTypes = setOf("SANDY_LOAM", "ALLUVIAL", "BLACK_COTTON"),
            minPh = 6.5, maxPh = 7.5,
            minTemp = 15.0, maxTemp = 32.0,
            waterNeed = "Medium (Drip / Micro-sprinkler)",
            expectedYield = "110-140 Quintals / Acre",
            residueBiomassPerAcreTons = 1.8,
            residueMarketPricePerTon = 1900,
            residueName = "Dried Outer Peels & Compost Biomass",
            agronomicRationale = "High revenue potential cash crop in western Maharashtra with high terminal APMC liquidity.",
            keyAdvantages = listOf(
                "Substantial revenue potential per acre with export-grade bulbs",
                "Short turnaround with steady market offloading",
                "Responsive to precision drip fertigation"
            ),
            commonRisks = listOf(
                "Purple blotch and Stemphylium blight in high morning humidity",
                "Price volatility at terminal mandi markets"
            )
        ),
        CropSpecification(
            cropName = "Soybean (JS 335 / KDS 726)",
            categoryName = "Oilseeds",
            season = AgriSeason.KHARIF,
            preferredSoilTypes = setOf("BLACK_COTTON", "CLAY_LOAM", "ALLUVIAL"),
            minPh = 6.2, maxPh = 7.5,
            minTemp = 20.0, maxTemp = 36.0,
            waterNeed = "Medium (Monsoon Rainfed)",
            expectedYield = "10-14 Quintals / Acre",
            residueBiomassPerAcreTons = 1.5,
            residueMarketPricePerTon = 4500,
            residueName = "Protein-Rich Fodder Straw",
            agronomicRationale = "Fast-maturing 95-day pulse that restores soil microbial balance and yields monetizable fodder straw.",
            keyAdvantages = listOf(
                "Short 95 to 100 day cycle freeing land for early Rabi sowing",
                "Root nodules fix atmospheric nitrogen for subsequent crop cycles",
                "Straw is highly monetizable for dairy feeding"
            ),
            commonRisks = listOf(
                "Susceptible to yellow mosaic virus transmitted by whiteflies",
                "Pod shattering if harvesting is delayed past peak maturity"
            )
        ),
        CropSpecification(
            cropName = "Bt Cotton (Bollgard II)",
            categoryName = "Fiber",
            season = AgriSeason.KHARIF,
            preferredSoilTypes = setOf("BLACK_COTTON", "CLAY_LOAM"),
            minPh = 6.5, maxPh = 8.2,
            minTemp = 22.0, maxTemp = 40.0,
            waterNeed = "Medium (Deep Taproot)",
            expectedYield = "12-16 Quintals / Acre",
            residueBiomassPerAcreTons = 2.8,
            residueMarketPricePerTon = 2800,
            residueName = "Hardwood Stalks for Bio-Pellets",
            agronomicRationale = "Deep root penetration in heavy black soils with high biomass briquetting monetization potential.",
            keyAdvantages = listOf(
                "Taproot extracts moisture and nutrients from subsoil layers",
                "Consistent industrial buyer demand from regional spinning mills",
                "Woody residue stalks generate solid bio-energy revenue"
            ),
            commonRisks = listOf(
                "Pink bollworm monitoring required via pheromone traps",
                "Labor intensive manual picking cycles"
            )
        ),
        CropSpecification(
            cropName = "Tomato (Abhinav / US 440)",
            categoryName = "Vegetables",
            season = AgriSeason.RABI,
            preferredSoilTypes = setOf("SANDY_LOAM", "ALLUVIAL", "RED_YELLOW"),
            minPh = 6.0, maxPh = 7.4,
            minTemp = 16.0, maxTemp = 32.0,
            waterNeed = "Medium (Drip Fertigation)",
            expectedYield = "16-22 Tons / Acre",
            residueBiomassPerAcreTons = 2.0,
            residueMarketPricePerTon = 2100,
            residueName = "Green Foliage for Bio-Compost",
            agronomicRationale = "Rapid turnaround horticulture crop with recurring weekly cash inflows during winter.",
            keyAdvantages = listOf(
                "Weekly harvesting provides continuous liquidity",
                "High returns on micro-irrigation investments",
                "Intercropping with marigold minimizes root-knot nematode load"
            ),
            commonRisks = listOf(
                "Early blight incidence during cool morning dew hours",
                "Blossom end rot in deficient calcium regimes"
            )
        ),
        CropSpecification(
            cropName = "Sugarcane (Co 86032 / Co 0238)",
            categoryName = "Cash Crop",
            season = AgriSeason.KHARIF,
            preferredSoilTypes = setOf("BLACK_COTTON", "ALLUVIAL", "CLAY_LOAM"),
            minPh = 6.5, maxPh = 8.0,
            minTemp = 20.0, maxTemp = 38.0,
            waterNeed = "High (Perennial Irrigation)",
            expectedYield = "45-60 Tons / Acre",
            residueBiomassPerAcreTons = 8.5,
            residueMarketPricePerTon = 3200,
            residueName = "Bagasse & Dry Trash Bio-Fuel",
            agronomicRationale = "Massive biomass output with assured factory procurement and cogeneration fuel demand.",
            keyAdvantages = listOf(
                "Guaranteed mill offloading contracts with cooperative sugar factories",
                "Dry trash and bagasse command top boiler co-firing rates",
                "Resilient to severe winds once cane stalk elongates"
            ),
            commonRisks = listOf(
                "Year-round irrigation requirement",
                "White grub and early shoot borer vulnerability"
            )
        ),
        CropSpecification(
            cropName = "Maize / Sweet Corn (Syngenta)",
            categoryName = "Grains",
            season = AgriSeason.KHARIF,
            preferredSoilTypes = setOf("ALLUVIAL", "RED_YELLOW", "BLACK_COTTON"),
            minPh = 6.0, maxPh = 7.5,
            minTemp = 18.0, maxTemp = 36.0,
            waterNeed = "Medium",
            expectedYield = "25-35 Quintals / Acre",
            residueBiomassPerAcreTons = 2.5,
            residueMarketPricePerTon = 3100,
            residueName = "Dry Stover & Cob Biomass",
            agronomicRationale = "Versatile grain; dry stover serves as high energy cattle silage and biofuel feedstock.",
            keyAdvantages = listOf(
                "Flexible sowing window across multiple seasons",
                "Stover and cobs command reliable poultry and ethanol feed contracts",
                "Rapid solar radiation conversion and vegetative growth"
            ),
            commonRisks = listOf(
                "Fall armyworm incidence during vegetative whorl phase",
                "Water logging sensitivity during early seedling stage"
            )
        )
    )

    fun getCatalogCropNames(): List<String> = cropCatalog.map { it.cropName }

    fun findSpecification(cropName: String): CropSpecification {
        return cropCatalog.firstOrNull {
            it.cropName.contains(cropName, ignoreCase = true) || cropName.contains(it.cropName.substringBefore(" "), ignoreCase = true)
        } ?: cropCatalog.first()
    }

    fun evaluateCompositeSuitability(
        farm: Farm?,
        soilProfile: Any?,
        weather: WeatherData?,
        previousCrops: List<String> = emptyList(),
        marketTrends: Map<String, AdvisoryPriceTrend> = emptyMap()
    ): List<CropSuitabilitySummary> {
        val currentSeason = getCurrentAgriSeason()
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        val farmSoil = farm?.soilType?.name ?: "BLACK_COTTON"
        val soilPh = 7.2
        val ambientTemp = weather?.temperatureCelsius ?: 26.5
        val rainMm = weather?.precipitationMm ?: 0.0
        val precedingCrop = previousCrops.firstOrNull()?.lowercase() ?: "cotton"

        return cropCatalog.map { spec ->
            computeSummaryForSpec(
                spec = spec,
                currentSeason = currentSeason,
                currentMonth = currentMonth,
                farmSoil = farmSoil,
                soilPh = soilPh,
                ambientTemp = ambientTemp,
                rainMm = rainMm,
                precedingCrop = precedingCrop,
                marketTrend = marketTrends[spec.cropName] ?: AdvisoryPriceTrend.RISING
            )
        }.sortedByDescending { it.compositeScore }
    }

    fun assessSpecificCrop(
        cropName: String,
        farm: Farm?,
        soilProfile: Any?,
        weather: WeatherData?,
        previousCrops: List<String> = emptyList(),
        marketTrend: AdvisoryPriceTrend = AdvisoryPriceTrend.RISING
    ): CropSuitabilitySummary {
        val spec = findSpecification(cropName)
        val currentSeason = getCurrentAgriSeason()
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        val farmSoil = farm?.soilType?.name ?: "BLACK_COTTON"
        val soilPh = 7.2
        val ambientTemp = weather?.temperatureCelsius ?: 26.5
        val rainMm = weather?.precipitationMm ?: 0.0
        val precedingCrop = previousCrops.firstOrNull()?.lowercase() ?: "cotton"

        return computeSummaryForSpec(
            spec = spec,
            currentSeason = currentSeason,
            currentMonth = currentMonth,
            farmSoil = farmSoil,
            soilPh = soilPh,
            ambientTemp = ambientTemp,
            rainMm = rainMm,
            precedingCrop = precedingCrop,
            marketTrend = marketTrend
        )
    }

    private fun computeSummaryForSpec(
        spec: CropSpecification,
        currentSeason: AgriSeason,
        currentMonth: Int,
        farmSoil: String,
        soilPh: Double,
        ambientTemp: Double,
        rainMm: Double,
        precedingCrop: String,
        marketTrend: AdvisoryPriceTrend
    ): CropSuitabilitySummary {
        val weatherScore = when {
            ambientTemp in spec.minTemp..spec.maxTemp && rainMm <= 20.0 -> 20
            Math.abs(ambientTemp - spec.minTemp) <= 3.0 || Math.abs(ambientTemp - spec.maxTemp) <= 3.0 -> 15
            ambientTemp < spec.minTemp - 5.0 || ambientTemp > spec.maxTemp + 5.0 -> 8
            else -> 12
        }

        val climateScore = when {
            spec.season == currentSeason -> 25
            spec.season.months.contains(currentMonth) -> 22
            (currentMonth + 1) % 12 in spec.season.months -> 18
            else -> 10
        }

        val soilTypePoints = when {
            spec.preferredSoilTypes.contains(farmSoil) -> 15
            farmSoil.contains("LOAM") && spec.preferredSoilTypes.any { it.contains("LOAM") } -> 12
            else -> 8
        }
        val phPoints = when {
            soilPh in spec.minPh..spec.maxPh -> 10
            Math.abs(soilPh - spec.minPh) <= 0.4 || Math.abs(soilPh - spec.maxPh) <= 0.4 -> 7
            else -> 4
        }
        val soilScore = (soilTypePoints + phPoints).coerceIn(0, 25)

        val isPrecedingOnion = precedingCrop.contains("onion") || precedingCrop.contains("garlic")
        val isCurrentOnion = spec.cropName.contains("onion", ignoreCase = true)
        val isPrecedingCotton = precedingCrop.contains("cotton")

        val rotationScore = when {
            isPrecedingCotton && (spec.cropName.contains("Chickpea") || spec.cropName.contains("Gram") || spec.cropName.contains("Soybean")) -> 15
            precedingCrop.isBlank() -> 10
            isPrecedingOnion && isCurrentOnion -> 4
            precedingCrop.contains(spec.cropName.substringBefore(" ").lowercase()) -> 5
            isPrecedingOnion -> 13
            else -> 12
        }

        val marketScore = when (marketTrend) {
            AdvisoryPriceTrend.RISING -> 15
            AdvisoryPriceTrend.STABLE -> 12
            AdvisoryPriceTrend.VOLATILE -> 9
            AdvisoryPriceTrend.FALLING -> 6
        }

        var riskPenalty = 0
        val caveats = mutableListOf<String>()

        if (isPrecedingOnion && isCurrentOnion) {
            riskPenalty += 12
            caveats.add("Consecutive onion sowing increases soil basal rot and salt buildup risks.")
        }
        if (climateScore <= 10) {
            riskPenalty += 8
            caveats.add("Off-season cycle: requires controlled micro-climate and pest monitoring.")
        }
        if (soilPh < spec.minPh) {
            riskPenalty += 4
            caveats.add("Slightly acidic soil: incorporate agricultural lime.")
        } else if (soilPh > spec.maxPh) {
            riskPenalty += 4
            caveats.add("Alkaline soil: supplement with organic compost and gypsum.")
        }

        val totalRaw = weatherScore + climateScore + soilScore + rotationScore + marketScore - riskPenalty
        val compositeScore = totalRaw.coerceIn(38, 98)

        val rotationNote = if (isPrecedingCotton && (spec.cropName.contains("Chickpea") || spec.cropName.contains("Gram"))) {
            "Optimal next-season nitrogen-fixing pulse following cotton harvest."
        } else {
            "Matches regional agro-climatic corridor conditions."
        }

        val mainRationale = "${spec.agronomicRationale} $rotationNote"

        return CropSuitabilitySummary(
            cropName = spec.cropName,
            categoryName = spec.categoryName,
            compositeScore = compositeScore,
            weatherScore = weatherScore,
            climateScore = climateScore,
            soilScore = soilScore,
            rotationScore = rotationScore,
            marketScore = marketScore,
            primaryRecommendationRationale = mainRationale,
            agronomicAdvantages = spec.keyAdvantages,
            riskCaveats = caveats.ifEmpty { listOf("Apply balanced basal N-P-K dosages and monitor soil moisture.") },
            waterNeed = spec.waterNeed,
            expectedYieldRange = spec.expectedYield
        )
    }
}

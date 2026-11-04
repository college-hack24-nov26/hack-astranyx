package com.sktech.wastetrack.domain.agro

import com.sktech.wastetrack.domain.agro.model.CropResiduePlan
import com.sktech.wastetrack.domain.agro.model.IndicativeEconomicsSummary

object AgroEconomicsEngine {

    data class CropEconomicsProfile(
        val cropName: String,
        val defaultYieldPerAcreQuintals: Double,
        val defaultResidueTonsPerAcre: Double,
        val defaultResidueRatePerTon: Int,
        val residueName: String,
        val seedCostNorm: Double,
        val fertilizerNutrientCostNorm: Double,
        val cropProtectionCostNorm: Double,
        val irrigationElectricityCostNorm: Double,
        val laborHarvestCostNorm: Double,
        val transportCostNorm: Double,
        val residueHandlingGuidance: String
    )

    private val cropEconomicsCatalog = mapOf(
        "Wheat" to CropEconomicsProfile(
            cropName = "Wheat (Lokwan / Sharbati / GW 322)",
            defaultYieldPerAcreQuintals = 20.0,
            defaultResidueTonsPerAcre = 2.2,
            defaultResidueRatePerTon = 5500,
            residueName = "Dry Cattle Straw (Bhusa)",
            seedCostNorm = 3200.0,
            fertilizerNutrientCostNorm = 4200.0,
            cropProtectionCostNorm = 1500.0,
            irrigationElectricityCostNorm = 2400.0,
            laborHarvestCostNorm = 5500.0,
            transportCostNorm = 1200.0,
            residueHandlingGuidance = "Bale straw with moisture under 12%. High demand from local dairy co-operatives."
        ),
        "Chickpea" to CropEconomicsProfile(
            cropName = "Chickpea / Bengal Gram (Chana - Digvijay)",
            defaultYieldPerAcreQuintals = 11.0,
            defaultResidueTonsPerAcre = 1.1,
            defaultResidueRatePerTon = 4200,
            residueName = "Gram Husk & Fodder Chaff",
            seedCostNorm = 3500.0,
            fertilizerNutrientCostNorm = 2400.0,
            cropProtectionCostNorm = 1800.0,
            irrigationElectricityCostNorm = 1400.0,
            laborHarvestCostNorm = 4800.0,
            transportCostNorm = 1000.0,
            residueHandlingGuidance = "Nutritious fodder supplement for ruminants; rich in crude protein."
        ),
        "Mustard" to CropEconomicsProfile(
            cropName = "Mustard (Pusa Bold / Varuna)",
            defaultYieldPerAcreQuintals = 10.0,
            defaultResidueTonsPerAcre = 1.4,
            defaultResidueRatePerTon = 2600,
            residueName = "Stalks for Biomass Briquettes",
            seedCostNorm = 1200.0,
            fertilizerNutrientCostNorm = 3100.0,
            cropProtectionCostNorm = 1400.0,
            irrigationElectricityCostNorm = 1200.0,
            laborHarvestCostNorm = 4200.0,
            transportCostNorm = 900.0,
            residueHandlingGuidance = "Fibrous woody stalks ideal for industrial heating pellet manufacture."
        ),
        "Onion" to CropEconomicsProfile(
            cropName = "Onion (Nashik Red / AgriFound Light Red)",
            defaultYieldPerAcreQuintals = 125.0,
            defaultResidueTonsPerAcre = 1.8,
            defaultResidueRatePerTon = 1900,
            residueName = "Dried Outer Peels & Foliage",
            seedCostNorm = 7500.0,
            fertilizerNutrientCostNorm = 8500.0,
            cropProtectionCostNorm = 4200.0,
            irrigationElectricityCostNorm = 3800.0,
            laborHarvestCostNorm = 12500.0,
            transportCostNorm = 3500.0,
            residueHandlingGuidance = "High sulfur and potassium content; ideal for rapid compost blend."
        ),
        "Soybean" to CropEconomicsProfile(
            cropName = "Soybean (JS 335 / KDS 726)",
            defaultYieldPerAcreQuintals = 12.0,
            defaultResidueTonsPerAcre = 1.5,
            defaultResidueRatePerTon = 4500,
            residueName = "Protein-Rich Fodder Straw",
            seedCostNorm = 3600.0,
            fertilizerNutrientCostNorm = 3200.0,
            cropProtectionCostNorm = 2200.0,
            irrigationElectricityCostNorm = 1000.0,
            laborHarvestCostNorm = 4600.0,
            transportCostNorm = 1100.0,
            residueHandlingGuidance = "High protein cattle feed; protect from dampness post-combine harvest."
        ),
        "Cotton" to CropEconomicsProfile(
            cropName = "Bt Cotton (Bollgard II)",
            defaultYieldPerAcreQuintals = 14.0,
            defaultResidueTonsPerAcre = 2.8,
            defaultResidueRatePerTon = 2800,
            residueName = "Hardwood Stalks for Bio-Pellets",
            seedCostNorm = 2600.0,
            fertilizerNutrientCostNorm = 6200.0,
            cropProtectionCostNorm = 5500.0,
            irrigationElectricityCostNorm = 3200.0,
            laborHarvestCostNorm = 8500.0,
            transportCostNorm = 1800.0,
            residueHandlingGuidance = "Chop with tractor slasher; top tier feedstock for industrial biomass gasification."
        ),
        "Tomato" to CropEconomicsProfile(
            cropName = "Tomato (Abhinav / US 440)",
            defaultYieldPerAcreQuintals = 180.0,
            defaultResidueTonsPerAcre = 2.0,
            defaultResidueRatePerTon = 2100,
            residueName = "Green Foliage for Bio-Compost",
            seedCostNorm = 5500.0,
            fertilizerNutrientCostNorm = 7800.0,
            cropProtectionCostNorm = 4800.0,
            irrigationElectricityCostNorm = 3200.0,
            laborHarvestCostNorm = 9500.0,
            transportCostNorm = 2800.0,
            residueHandlingGuidance = "Crush and compost immediately with microbial inoculants."
        ),
        "Sugarcane" to CropEconomicsProfile(
            cropName = "Sugarcane (Co 86032 / Co 0238)",
            defaultYieldPerAcreQuintals = 500.0,
            defaultResidueTonsPerAcre = 8.5,
            defaultResidueRatePerTon = 3200,
            residueName = "Bagasse & Dry Trash Bio-Fuel",
            seedCostNorm = 8200.0,
            fertilizerNutrientCostNorm = 9800.0,
            cropProtectionCostNorm = 3500.0,
            irrigationElectricityCostNorm = 6500.0,
            laborHarvestCostNorm = 14000.0,
            transportCostNorm = 4500.0,
            residueHandlingGuidance = "Shred trash with stubble mulcher; highly valued by boiler cogen plants."
        ),
        "Maize" to CropEconomicsProfile(
            cropName = "Maize / Sweet Corn (Syngenta)",
            defaultYieldPerAcreQuintals = 30.0,
            defaultResidueTonsPerAcre = 2.5,
            defaultResidueRatePerTon = 3100,
            residueName = "Dry Stover & Cob Biomass",
            seedCostNorm = 3100.0,
            fertilizerNutrientCostNorm = 4600.0,
            cropProtectionCostNorm = 1900.0,
            irrigationElectricityCostNorm = 2200.0,
            laborHarvestCostNorm = 5200.0,
            transportCostNorm = 1400.0,
            residueHandlingGuidance = "Stover is ideal for silage feed or ethanol refinery supply."
        )
    )

    fun findProfile(cropName: String): CropEconomicsProfile {
        return cropEconomicsCatalog.entries.firstOrNull {
            cropName.contains(it.key, ignoreCase = true)
        }?.value ?: cropEconomicsCatalog["Wheat"]!!
    }

    fun computeIndicativeEconomics(
        cropName: String,
        acreage: Double = 1.0,
        modalPricePerQuintal: Double,
        customSeedCost: Double? = null,
        customFertilizerCost: Double? = null,
        customLaborCost: Double? = null
    ): IndicativeEconomicsSummary {
        val profile = findProfile(cropName)
        val validAcreage = if (acreage > 0.0) acreage else 1.0

        val yieldQuintals = profile.defaultYieldPerAcreQuintals * validAcreage
        val grossCropRevenue = yieldQuintals * modalPricePerQuintal

        val residueTons = profile.defaultResidueTonsPerAcre * validAcreage
        val grossResidueRevenue = residueTons * profile.defaultResidueRatePerTon

        val totalGrossRevenue = grossCropRevenue + grossResidueRevenue

        val seedCost = (customSeedCost ?: profile.seedCostNorm) * validAcreage
        val fertCost = (customFertilizerCost ?: profile.fertilizerNutrientCostNorm) * validAcreage
        val protCost = profile.cropProtectionCostNorm * validAcreage
        val irrigCost = profile.irrigationElectricityCostNorm * validAcreage
        val laborCost = (customLaborCost ?: profile.laborHarvestCostNorm) * validAcreage
        val transpCost = profile.transportCostNorm * validAcreage

        val totalProductionCost = seedCost + fertCost + protCost + irrigCost + laborCost + transpCost
        val expectedNet = totalGrossRevenue - totalProductionCost

        val conservativeGross = totalGrossRevenue * 0.85
        val conservativeCost = totalProductionCost * 1.10
        val conservativeNet = conservativeGross - conservativeCost

        val optimisticGross = totalGrossRevenue * 1.15
        val optimisticCost = totalProductionCost * 0.95
        val optimisticNet = optimisticGross - optimisticCost

        val isDefaultCosts = (customSeedCost == null && customFertilizerCost == null && customLaborCost == null)
        val costNote = if (isDefaultCosts) {
            "Benchmark input costs from State Agricultural University standards."
        } else {
            "Input costs customized by farmer plot expenditure inputs."
        }

        return IndicativeEconomicsSummary(
            currency = "INR",
            acreage = validAcreage,
            expectedYieldQuintals = yieldQuintals,
            forecastModalRatePerQuintal = modalPricePerQuintal,
            grossCropRevenue = grossCropRevenue,
            residueBiomassTons = residueTons,
            residueValuePerTon = profile.defaultResidueRatePerTon.toDouble(),
            grossResidueRevenue = grossResidueRevenue,
            totalGrossRevenue = totalGrossRevenue,
            seedCost = seedCost,
            fertilizerNutrientCost = fertCost,
            cropProtectionCost = protCost,
            irrigationElectricityCost = irrigCost,
            laborHarvestCost = laborCost,
            logisticsTransportCost = transpCost,
            totalProductionCost = totalProductionCost,
            conservativeNetProfit = conservativeNet,
            expectedNetProfit = expectedNet,
            optimisticNetProfit = optimisticNet,
            costSourceNote = costNote
        )
    }

    fun buildResiduePlan(cropName: String, acreage: Double = 1.0): CropResiduePlan {
        val profile = findProfile(cropName)
        val validAcreage = if (acreage > 0.0) acreage else 1.0
        return CropResiduePlan(
            residueName = profile.residueName,
            estimatedBiomassTonsPerAcre = profile.defaultResidueTonsPerAcre * validAcreage,
            suggestedIndustrialApplication = profile.residueHandlingGuidance,
            estimatedResidueValuePerTonInr = profile.defaultResidueRatePerTon.toDouble(),
            inSituManagementAlternative = "Mulch with tractor happy seeder or bio-decomposer to restore soil carbon."
        )
    }
}

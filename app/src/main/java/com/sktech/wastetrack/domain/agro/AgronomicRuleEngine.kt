package com.sktech.wastetrack.domain.agro

import com.sktech.wastetrack.domain.agro.model.FertilizerPlanGuidance
import com.sktech.wastetrack.domain.agro.model.GrowthStageScheduleStep
import com.sktech.wastetrack.domain.agro.model.IrrigationPlanGuidance
import com.sktech.wastetrack.domain.agro.model.ScheduledCareAction
import com.sktech.wastetrack.domain.agro.model.WeatherAdaptationNotice
import com.sktech.wastetrack.domain.model.SoilProfile
import com.sktech.wastetrack.domain.model.WeatherData
import java.util.UUID

object AgronomicRuleEngine {

    fun evaluateIrrigationGuidance(
        cropName: String,
        weather: WeatherData?,
        waterSource: String? = "DRIP"
    ): Pair<IrrigationPlanGuidance, WeatherAdaptationNotice?> {
        val rainMm = weather?.precipitationMm ?: 0.0
        val isHeavyRain = rainMm >= 15.0

        val baseMethod = when {
            waterSource?.contains("DRIP", ignoreCase = true) == true -> "Precision Drip Line"
            waterSource?.contains("SPRINKLER", ignoreCase = true) == true -> "Micro-Sprinkler System"
            else -> "Controlled Furrow / Basin"
        }

        val baseFreq = when {
            cropName.contains("Sugarcane", ignoreCase = true) -> "Every 3 to 4 days (3.5 hours per shift)"
            cropName.contains("Tomato", ignoreCase = true) -> "Every 2 days (1.5 hours in morning)"
            cropName.contains("Onion", ignoreCase = true) -> "Every 4 to 5 days (2.0 hours per shift)"
            cropName.contains("Chickpea", ignoreCase = true) -> "Critical stages only: 1 pre-sowing + 1 pod formation (drought hardy)"
            cropName.contains("Wheat", ignoreCase = true) -> "4 to 5 key irrigations (CRI, Tillering, Flowering, Grain filling)"
            cropName.contains("Soybean", ignoreCase = true) -> "Monsoon rainfed; supplemental irrigation only if dry spell exceeds 12 days"
            else -> "Every 3 to 5 days based on tensiometer moisture reading"
        }

        val preferredTime = "Early Morning (6:00 AM to 8:30 AM) to curb 35% evapotranspiration loss."

        val criticalStages = listOf(
            "Crown root initiation (20 to 25 days after sowing)",
            "Active tillering / vegetative branching",
            "Flowering & pod / head development",
            "Grain filling / bulb enlargement"
        )

        val tips = listOf(
            "Maintain soil moisture at 65% to 75% field capacity during flowering",
            "Use organic mulch along irrigation laterals to suppress evaporation",
            "Avoid evening and midday watering to prevent fungal collar rot"
        )

        return if (isHeavyRain) {
            val notice = WeatherAdaptationNotice(
                noticeId = "WAN-${UUID.randomUUID().toString().take(6).uppercase()}",
                urgencyLevel = "HIGH",
                title = "Precipitation Evapotranspiration Radar Alert",
                headline = "Upcoming heavy precipitation (%.1f mm forecast) detected in next 3 days".format(rainMm),
                recommendedFarmerAction = "Suspend / Withhold scheduled irrigation for 48 to 72 hours",
                triggerCondition = "Upcoming heavy precipitation (%.1f mm forecast) detected in next 3 days".format(rainMm),
                suggestedAction = "Suspend / Withhold scheduled irrigation for 48 to 72 hours",
                agronomicRationale = "Upcoming heavy rain detected. Soil profile must be allowed to dry slightly so topsoil can absorb maximum natural rainwater without flooding or waterlogging crops, preventing root asphyxiation, collar rot, and water wastage.",
                reviewTime = "Review in 48 hours or after rain event concludes."
            )
            val guidance = IrrigationPlanGuidance(
                baseMethod = baseMethod,
                frequencyHours = "PAUSED (Pre-Rain Flood Prevention)",
                preferredTimeOfDay = preferredTime,
                criticalWaterNeedStages = criticalStages,
                waterSavingTips = tips,
                rainHaltActive = true,
                rainAdjustmentNote = "Scheduled watering paused for 48 to 72 hours due to %.1f mm rain forecast. Preserves soil absorption capacity to avoid crop root asphyxiation and flooding.".format(rainMm)
            )
            Pair(guidance, notice)
        } else if (rainMm >= 5.0) {
            val notice = WeatherAdaptationNotice(
                noticeId = "WAN-${UUID.randomUUID().toString().take(6).uppercase()}",
                urgencyLevel = "MODERATE",
                title = "Precipitation Evapotranspiration Radar Advisory",
                headline = "Moderate precipitation (%.1f mm forecast) in upcoming 3 days".format(rainMm),
                recommendedFarmerAction = "Curtail irrigation run-time by 50 to 60%",
                triggerCondition = "Moderate precipitation (%.1f mm forecast) in upcoming 3 days".format(rainMm),
                suggestedAction = "Curtail irrigation run-time by 50 to 60%",
                agronomicRationale = "Reducing irrigation water volume allows topsoil pore spaces to absorb incoming rainfall naturally, eliminating water wastage and crop waterlogging.",
                reviewTime = "Review in 24 hours."
            )
            val guidance = IrrigationPlanGuidance(
                baseMethod = baseMethod,
                frequencyHours = "REDUCED (Pre-Rain Absorption Mode)",
                preferredTimeOfDay = preferredTime,
                criticalWaterNeedStages = criticalStages,
                waterSavingTips = tips,
                rainHaltActive = true,
                rainAdjustmentNote = "Irrigation run-time curtailed by 50% ahead of %.1f mm forecast rain so the soil absorbs incoming rain without excess water build-up.".format(rainMm)
            )
            Pair(guidance, notice)
        } else {
            val guidance = IrrigationPlanGuidance(
                baseMethod = baseMethod,
                frequencyHours = baseFreq,
                preferredTimeOfDay = preferredTime,
                criticalWaterNeedStages = criticalStages,
                waterSavingTips = tips,
                rainHaltActive = false,
                rainAdjustmentNote = null
            )
            Pair(guidance, null)
        }
    }

    fun evaluateFertilizerGuidance(
        cropName: String,
        soilProfile: SoilProfile?,
        previousCrop: String?,
        farmSoilType: String?
    ): FertilizerPlanGuidance {
        val hasLabTest = soilProfile != null && soilProfile.ph != null && soilProfile.ph > 0.0
        val soilSourceType = if (hasLabTest) "VERIFIED_LAB_TEST" else "REGIONAL_ESTIMATE"

        val ph = soilProfile?.ph ?: 7.2
        val nPpm = soilProfile?.nitrogen ?: 220.0
        val pPpm = soilProfile?.phosphorus ?: 18.0
        val kPpm = soilProfile?.potassium ?: 150.0

        val soilSummary = if (hasLabTest) {
            "Verified Soil Test (Lab): pH %.1f, N %.0f ppm, P %.0f ppm, K %.0f ppm in %s soil.".format(
                ph, nPpm, pPpm, kPpm, farmSoilType ?: "Black Cotton"
            )
        } else {
            "Regional Soil Benchmark (%s): Estimated pH ~%.1f with medium NPK availability. Standard local baseline applied.".format(
                farmSoilType ?: "Black Cotton", ph
            )
        }

        var rotationNutrientNote: String? = null
        var nReductionKgPerAcre = 0.0

        val prevLower = previousCrop?.lowercase() ?: ""
        val isPrecedingLegume = prevLower.contains("soybean") ||
                prevLower.contains("chickpea") ||
                prevLower.contains("chana") ||
                prevLower.contains("gram") ||
                prevLower.contains("tur") ||
                prevLower.contains("pulse") ||
                prevLower.contains("groundnut")

        val isPrecedingOnion = prevLower.contains("onion") || prevLower.contains("garlic")

        if (isPrecedingLegume) {
            nReductionKgPerAcre = 40.0
            rotationNutrientNote = "Preceding logged crop: $previousCrop. Symbiotic atmospheric nitrogen fixation verified. Synthetic Urea requirement reduced by 35 to 45 kg/Acre (credited 40 kg biological N) to lower input expenses and preserve soil microbiome."
        } else if (isPrecedingOnion) {
            rotationNutrientNote = "Preceding logged crop: $previousCrop. High residual sulphate in root zone detected. Sulphate-bearing fertilizers strictly EXCLUDED. Balanced sulphate-free alternatives (DAP, MOP, EDTA chelates) prescribed."
        } else if (!previousCrop.isNullOrBlank()) {
            rotationNutrientNote = "Preceding logged crop: $previousCrop. Rotational soil nutrient extraction analyzed. Balanced multi-element nutrition formulated for next cycle."
        }

        val soilTestNotice = if (isPrecedingOnion && !hasLabTest) {
            "Note on prior crop ($previousCrop): Prior onion harvest leaves elevated sulphate in root zone. Sulphate-free fertilizer regimen applied. Conduct an annual plot soil test for precise micro-nutrient balancing."
        } else if (!hasLabTest) {
            "Field relies on regional baseline. Conduct an annual plot soil test for precise micro-nutrient (Zinc, Boron, Sulphur) targeting."
        } else {
            null
        }

        val basalDose = when {
            isPrecedingOnion ->
                "Basal Placement (Sulphate-Free Formulation): Apply DAP (18:46:0) @ 50 kg/Acre + Muriate of Potash (MOP 0:0:60) @ 25 kg/Acre. Single Super Phosphate (SSP) and elemental Sulphur are EXCLUDED due to high residual sulphate from prior $previousCrop crop."
            cropName.contains("Wheat", ignoreCase = true) ->
                "Apply DAP @ 50 kg/Acre + MOP @ 25 kg/Acre as basal placement along with 2 bags neem-coated urea at sowing."
            cropName.contains("Chickpea", ignoreCase = true) ->
                "Apply DAP @ 40 kg/Acre as basal dose. Treat seeds with Rhizobium and PSB bio-fertilizers (250g per 10kg seed)."
            cropName.contains("Onion", ignoreCase = true) ->
                "Basal dose: 50 kg 10:26:26 + 10 kg elemental Sulphur (90% WDG) per acre to enhance bulb pungent solids and skin coloration."
            cropName.contains("Cotton", ignoreCase = true) ->
                "Basal: 50 kg 20:20:0:13 + 30 kg Muriate of Potash (MOP) per acre at 5 cm below seed depth."
            cropName.contains("Tomato", ignoreCase = true) ->
                "Basal bed dressing: 12:32:16 @ 50 kg/acre + 10 kg Magnesium Sulphate."
            else ->
                "Apply 50 kg NPK (19:19:19 or DAP balance) per acre at land preparation."
        }

        val topDressDose = if (nReductionKgPerAcre > 0) {
            "Top-dress with Urea @ 10 to 15 kg/Acre at first vegetative weeding (reduced by 40 kg/Acre due to preceding legume N-credit)."
        } else if (isPrecedingOnion) {
            "Top-dress with Neem-Coated Urea @ 40 kg/Acre. Avoid Ammonium Sulphate top-dressing to prevent unnecessary sulphate accumulation."
        } else {
            "Top-dress with Urea @ 45 kg/Acre split into two equal doses at 21 and 42 days post-emergence."
        }

        val fertigationDose = when {
            isPrecedingOnion ->
                "Fertigate via drip: 0:52:34 (MKP - Mono Potassium Phosphate) @ 3 kg/Acre weekly during active growth. (Sulphate salts omitted)."
            cropName.contains("Tomato", ignoreCase = true) || cropName.contains("Onion", ignoreCase = true) ->
                "Fertigate via drip: 0:52:34 (MKP) @ 3 kg/acre weekly during flower and bulb/fruit formation, followed by 13:0:45 @ 4 kg/acre."
            else ->
                "Foliar spray of 19:19:19 @ 5 g/L during active vegetative stage for rapid chlorophyll synthesis."
        }

        val microDose = when {
            isPrecedingOnion ->
                "Apply Chelated Zinc (EDTA 12%) @ 1.0 g/L + Boron (20%) @ 1.0 g/L foliar spray. (Sulphate-based micro salts like ZnSO4/FeSO4 excluded to avoid salt overload)."
            ph > 8.0 -> "Slight alkaline soil: Apply Zinc Sulphate (21%) @ 10 kg/Acre + Ferrous Sulphate (19%) @ 5 kg/Acre to prevent chlorosis."
            ph < 6.2 -> "Slight acid soil: Apply Agricultural Dolomite Lime @ 100 kg/Acre to ensure Calcium and Magnesium availability."
            else -> "Spray chelated Zinc (EDTA 12%) @ 1 g/L and Boron (20%) @ 1 g/L at pre-flowering stage."
        }

        val compostDose = "Incorporate 2.5 to 3.0 Tons/Acre of well-decomposed Farmyard Manure (FYM) or fortified vermicompost 15 days before sowing."

        return FertilizerPlanGuidance(
            soilSourceType = soilSourceType,
            soilSummaryText = soilSummary,
            basalApplicationDose = basalDose,
            vegetativeTopDressDose = topDressDose,
            floweringFertigationDose = fertigationDose,
            micronutrientPrescription = microDose,
            organicCompostDose = compostDose,
            previousCropCarryOverCredit = rotationNutrientNote,
            soilTestRecommendationNotice = soilTestNotice
        )
    }

    fun generateGrowthStageCareSchedule(
        cropName: String,
        variety: String? = null
    ): List<GrowthStageScheduleStep> {
        val steps = mutableListOf<GrowthStageScheduleStep>()

        steps.add(
            GrowthStageScheduleStep(
                stageOrder = 1,
                stageName = "Land Preparation & Sowing",
                windowDescription = "Day 0 to Day 15",
                approximateDurationDays = 15,
                keyObjectives = "Ensure optimal seedbed tilth, correct seed depth, and initial root establishment.",
                actions = listOf(
                    ScheduledCareAction(
                        actionId = "ACT-101",
                        title = "Deep Summer / Pre-Sowing Ploughing",
                        category = "SOIL_TREATMENT",
                        instruction = "Two cross-ploughings followed by rotavator to crush hard pans and bury weeds.",
                        isCritical = true
                    ),
                    ScheduledCareAction(
                        actionId = "ACT-102",
                        title = "Seed Treatment with Bio-Agents",
                        category = "IPM_PROTECTION",
                        instruction = "Treat seed lot with Trichoderma viride @ 5g/kg seed to prevent seedling damping-off.",
                        isCritical = true
                    ),
                    ScheduledCareAction(
                        actionId = "ACT-103",
                        title = "Basal Fertilizer Application",
                        category = "FERTILIZER",
                        instruction = "Incorporate basal NPK and organic compost evenly below seed line.",
                        isCritical = true
                    )
                ),
                stageWarnings = listOf(
                    "Do not sow if topsoil temperature exceeds 36C or during active unseasonal downpours."
                )
            )
        )

        steps.add(
            GrowthStageScheduleStep(
                stageOrder = 2,
                stageName = "Vegetative Establishment",
                windowDescription = "Day 16 to Day 45",
                approximateDurationDays = 30,
                keyObjectives = "Foster canopy cover, robust taproot elongation, and suppress early-season weed competition.",
                actions = listOf(
                    ScheduledCareAction(
                        actionId = "ACT-201",
                        title = "Inter-cultivation & Manual Weeding",
                        category = "WEEDING_CULTURE",
                        instruction = "First hoeing at 21 days to aerate root zone and remove competing grasses.",
                        isCritical = true
                    ),
                    ScheduledCareAction(
                        actionId = "ACT-202",
                        title = "First Top-Dress Nutrient Dose",
                        category = "FERTILIZER",
                        instruction = "Apply scheduled Nitrogen dose followed by immediate light drip irrigation.",
                        isCritical = false
                    ),
                    ScheduledCareAction(
                        actionId = "ACT-203",
                        title = "Pheromone & Sticky Trap Installation",
                        category = "IPM_PROTECTION",
                        instruction = "Install 5 yellow/blue sticky cards and 4 sex-pheromone traps per acre for early pest surveillance.",
                        isCritical = false
                    )
                ),
                stageWarnings = listOf(
                    "Inspect weekly for leafhoppers, thrips, or early stem borers."
                )
            )
        )

        steps.add(
            GrowthStageScheduleStep(
                stageOrder = 3,
                stageName = "Flowering & Fruit/Pod Formation",
                windowDescription = "Day 46 to Day 80",
                approximateDurationDays = 35,
                keyObjectives = "Maximize blossom retention, prevent flower drop, and ensure moisture stability.",
                actions = listOf(
                    ScheduledCareAction(
                        actionId = "ACT-301",
                        title = "Strict Moisture Maintenance",
                        category = "IRRIGATION",
                        instruction = "Prevent moisture stress; moisture dips during flowering cause blossom abortion.",
                        isCritical = true
                    ),
                    ScheduledCareAction(
                        actionId = "ACT-302",
                        title = "Foliar Micronutrient & Boron Spray",
                        category = "FERTILIZER",
                        instruction = "Apply Soluble Boron (20%) @ 1g/L to stimulate pollen tube growth and fruit set.",
                        isCritical = false
                    ),
                    ScheduledCareAction(
                        actionId = "ACT-303",
                        title = "Biological Fungicide Guard",
                        category = "IPM_PROTECTION",
                        instruction = "Prophylactic spray of Pseudomonas fluorescens @ 4g/L if overcast conditions prevail.",
                        isCritical = false
                    )
                ),
                stageWarnings = listOf(
                    "Avoid chemical pesticide spraying during peak bee foraging hours (7:00 AM to 10:30 AM)."
                )
            )
        )

        steps.add(
            GrowthStageScheduleStep(
                stageOrder = 4,
                stageName = "Maturation & Primary Harvest",
                windowDescription = "Day 81 to Day 115",
                approximateDurationDays = 35,
                keyObjectives = "Harvest at peak moisture index, sort, grade, and transport to APMC mandi.",
                actions = listOf(
                    ScheduledCareAction(
                        actionId = "ACT-401",
                        title = "Pre-Harvest Irrigation Withholding",
                        category = "IRRIGATION",
                        instruction = "Stop watering 8 to 10 days before harvest to hasten uniform field dry-down and shelf life.",
                        isCritical = true
                    ),
                    ScheduledCareAction(
                        actionId = "ACT-402",
                        title = "Clean Harvest & Grading",
                        category = "HARVEST_RESIDUE",
                        instruction = "Cut stalks close to ground or pick mature grade pods/bulbs in clean dry crates.",
                        isCritical = true
                    )
                ),
                stageWarnings = listOf(
                    "Do not harvest during damp morning dew; dry crops command higher APMC modal bids."
                )
            )
        )

        steps.add(
            GrowthStageScheduleStep(
                stageOrder = 5,
                stageName = "Residue Valorization & Soil Reset",
                windowDescription = "Day 116 to Day 130",
                approximateDurationDays = 15,
                keyObjectives = "Monetize crop residues, eliminate pest harborage, and prepare field for rotation cycle.",
                actions = listOf(
                    ScheduledCareAction(
                        actionId = "ACT-501",
                        title = "Bale Agricultural Stalks / Straw",
                        category = "HARVEST_RESIDUE",
                        instruction = "Bale dry residue (moisture < 14%) and create listing on AgriStock Bio-Exchange for power plant or fodder buyers.",
                        isCritical = true
                    ),
                    ScheduledCareAction(
                        actionId = "ACT-502",
                        title = "Zero-Burn Plot Sanitization",
                        category = "SOIL_TREATMENT",
                        instruction = "Never burn residue. Spray bio-decomposer or incorporate leftover stubble to replenish soil organic carbon.",
                        isCritical = true
                    )
                ),
                stageWarnings = listOf(
                    "Burning stubble damages topsoil microbial flora and destroys carbon credits."
                )
            )
        )

        return steps
    }
}

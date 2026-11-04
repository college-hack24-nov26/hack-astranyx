package com.sktech.wastetrack.ui.screens.agroplan

import com.sktech.wastetrack.domain.agro.AgronomicRuleEngine
import com.sktech.wastetrack.domain.model.SoilProfile
import com.sktech.wastetrack.domain.model.WeatherData

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.sktech.wastetrack.domain.agro.model.AgroPlanRevisionRecord
import com.sktech.wastetrack.domain.agro.model.CropResiduePlan
import com.sktech.wastetrack.domain.agro.model.CropSuitabilitySummary
import com.sktech.wastetrack.domain.agro.model.CustomAgroPlan
import com.sktech.wastetrack.domain.agro.model.DailyRainForecastItem
import com.sktech.wastetrack.domain.agro.model.FertilizerPlanGuidance
import com.sktech.wastetrack.domain.agro.model.GrowthStageScheduleStep
import com.sktech.wastetrack.domain.agro.model.IndicativeEconomicsSummary
import com.sktech.wastetrack.domain.agro.model.IrrigationPlanGuidance
import com.sktech.wastetrack.domain.agro.model.ScheduledCareAction
import com.sktech.wastetrack.domain.agro.model.WeatherAdaptationNotice
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class CustomAgroPlanUiState(
    val farmId: String = "default_farm",
    val farmName: String = "Sinnar Organic Parcel 01",
    val cropName: String = "Wheat",
    val isLoading: Boolean = false,
    val plan: CustomAgroPlan? = null,
    val completedActionIds: Set<String> = emptySet(),
    val revisions: List<AgroPlanRevisionRecord> = emptyList(),
    val isRevisionSheetOpen: Boolean = false,
    val isReviseDialogOpen: Boolean = false,
    val isWeatherNoticeDismissed: Boolean = false,
    val feedbackMessage: String? = null,
    val todayRainProbability: Int = 65,
    val todayPrecipitationMm: Double = 6.2,
    val floodOrHeavyRainWarning: String? = null,
    val isWaterReleaseSafe: Boolean = false,
    val dailyForecast: List<DailyRainForecastItem> = listOf(
        DailyRainForecastItem("Today", 65, 6.2, "Scattered Showers", 27.5),
        DailyRainForecastItem("Tomorrow", 30, 1.2, "Partly Cloudy", 29.0),
        DailyRainForecastItem("Day 3", 15, 0.0, "Clear Sky", 31.0),
        DailyRainForecastItem("Day 4", 10, 0.0, "Sunny", 31.5),
        DailyRainForecastItem("Day 5", 20, 0.4, "Clear Sky", 30.0),
        DailyRainForecastItem("Day 6", 50, 4.0, "Afternoon Rain", 28.0),
        DailyRainForecastItem("Day 7", 25, 0.8, "Partly Cloudy", 29.5)
    )
)

@HiltViewModel
class CustomAgroPlanViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val farmIdParam = savedStateHandle.get<String>("farmId")?.ifBlank { "default_farm" } ?: "default_farm"
    private val cropParam = savedStateHandle.get<String>("crop")?.ifBlank { "Wheat" } ?: "Wheat"

    private val _state = MutableStateFlow(
        CustomAgroPlanUiState(
            farmId = farmIdParam,
            cropName = cropParam
        )
    )
    val state: StateFlow<CustomAgroPlanUiState> = _state.asStateFlow()

    init {
        loadPlanForCrop(cropParam)
    }

    fun selectCrop(cropName: String) {
        _state.update { it.copy(cropName = cropName) }
        loadPlanForCrop(cropName)
    }

    private fun loadPlanForCrop(cropName: String) {
        val normalized = cropName.lowercase()
        val plan = when {
            normalized.contains("cotton") -> createCottonPlan()
            normalized.contains("sugarcane") || normalized.contains("bagasse") -> createSugarcanePlan()
            normalized.contains("soybean") -> createSoybeanPlan()
            else -> createWheatPlan()
        }

        val initialRevisions = listOf(
            AgroPlanRevisionRecord(
                revisionId = "REV-001",
                planVersion = 1,
                revisionTrigger = "INITIAL_GENERATION",
                changeSummary = "Baseline plan synthesized from localized soil test and IMD agro-met dataset",
                timestamp = System.currentTimeMillis() - 86400000L * 7
            )
        )

        _state.update {
            it.copy(
                plan = plan,
                revisions = initialRevisions,
                completedActionIds = setOf("act_001", "act_002")
            )
        }
    }

    fun toggleAction(actionId: String) {
        _state.update { current ->
            val set = current.completedActionIds.toMutableSet()
            if (set.contains(actionId)) {
                set.remove(actionId)
            } else {
                set.add(actionId)
            }
            current.copy(completedActionIds = set)
        }
    }

    fun toggleReviseDialog(open: Boolean) {
        _state.update { it.copy(isReviseDialogOpen = open) }
    }

    fun showRevisionSheet(open: Boolean) {
        _state.update { it.copy(isRevisionSheetOpen = open) }
    }

    fun dismissWeatherNotice() {
        _state.update { it.copy(isWeatherNoticeDismissed = true) }
    }

    fun clearFeedback() {
        _state.update { it.copy(feedbackMessage = null) }
    }

    fun submitRevision(notes: String) {
        _state.update { current ->
            val currentPlan = current.plan ?: return@update current
            val newVersion = currentPlan.planVersion + 1
            val updatedPlan = currentPlan.copy(
                planVersion = newVersion,
                updatedAt = System.currentTimeMillis()
            )
            val newRevision = AgroPlanRevisionRecord(
                revisionId = "REV-00$newVersion",
                planVersion = newVersion,
                revisionTrigger = "FARMER_CUSTOMIZATION",
                changeSummary = notes.ifBlank { "Agronomic dosage and schedule adjusted by farmer" },
                timestamp = System.currentTimeMillis()
            )
            current.copy(
                plan = updatedPlan,
                revisions = listOf(newRevision) + current.revisions,
                isReviseDialogOpen = false,
                feedbackMessage = "AgroPlan updated to Version $newVersion."
            )
        }
    }

    private fun createWheatPlan(): CustomAgroPlan {

        val (evaluatedIrrigation, adaptationNotice) = AgronomicRuleEngine.evaluateIrrigationGuidance(

            cropName = "Wheat",

            weather = WeatherData(precipitationMm = 18.5),

            waterSource = "DRIP"

        )

        val evaluatedFertilizer = AgronomicRuleEngine.evaluateFertilizerGuidance(

            cropName = "Wheat",

            soilProfile = SoilProfile(ph = 7.4, nitrogen = 240.0, phosphorus = 20.0, potassium = 160.0),

            previousCrop = "Soybean",

            farmSoilType = "Black Cotton"

        )

        val evaluatedCare = AgronomicRuleEngine.generateGrowthStageCareSchedule("Wheat")



        return CustomAgroPlan(

            id = "AGP-NSK-WHEAT-2026",

            farmId = farmIdParam,

            farmName = "Sinnar Organic Parcel 01",

            cropName = "Wheat",

            cropCategory = "Grain",

            variety = "GW 322 / Lokwan Premium",

            targetSeason = "Rabi 2025-2026",

            planVersion = 1,

            isLatest = true,

            suitabilitySummary = CropSuitabilitySummary(

                cropName = "Wheat (GW 322)",

                categoryName = "Cereal Grains",

                compositeScore = 94,

                weatherScore = 19,

                climateScore = 24,

                soilScore = 23,

                rotationScore = 14,

                marketScore = 14,

                primaryRecommendationRationale = "Optimal agro-climatic window for high-protein grain with robust biomass straw yield. Highly compatible following soybean harvest.",

                agronomicAdvantages = listOf(

                    "High tillering potential under medium black loamy soil",

                    "Strong industrial market for baled dry wheat straw residue",

                    "Low pest susceptibility during winter cool spell"

                ),

                riskCaveats = listOf(

                    "Sensitive to early heat stress during grain filling stage",

                    "Requires strict irrigation suspension during unseasonal rains"

                ),

                waterNeed = "400 - 450 mm across 5 critical growth stages",

                expectedYieldRange = "20 - 24 Quintals / Acre"

            ),

            economicsSnapshot = IndicativeEconomicsSummary(

                currency = "INR",

                acreage = 1.0,

                expectedYieldQuintals = 22.0,

                forecastModalRatePerQuintal = 2450.0,

                grossCropRevenue = 53900.0,

                residueBiomassTons = 2.4,

                residueValuePerTon = 3000.0,

                grossResidueRevenue = 7200.0,

                totalGrossRevenue = 61100.0,

                seedCost = 3200.0,

                fertilizerNutrientCost = 6400.0,

                cropProtectionCost = 2100.0,

                irrigationElectricityCost = 2800.0,

                laborHarvestCost = 9600.0,

                logisticsTransportCost = 2200.0,

                totalProductionCost = 26300.0,

                conservativeNetProfit = 28500.0,

                expectedNetProfit = 34800.0,

                optimisticNetProfit = 41200.0,

                costSourceNote = "Based on MPKV Rahuri Agricultural University benchmark norms"

            ),

            irrigationGuidance = evaluatedIrrigation,

            fertilizerGuidance = evaluatedFertilizer,

            careSchedule = evaluatedCare,

            residuePlan = CropResiduePlan(

                residueName = "Dry Wheat Straw / Stubble Residue",

                estimatedBiomassTonsPerAcre = 2.4,

                targetMoisturePercentMax = 14.0,

                suggestedIndustrialApplication = "Thermal Co-Firing Power Generation & Agro-Paper Pulp Mills",

                estimatedResidueValuePerTonInr = 3000.0,

                inSituManagementAlternative = "Incorporation with Super Seeder / Rotavator + PUSA Bio-Decomposer"

            ),

            activeAdaptationAlerts = if (adaptationNotice != null) listOf(adaptationNotice) else emptyList()

        )

    }

    private fun createCottonPlan(): CustomAgroPlan {

        val (evaluatedIrrigation, adaptationNotice) = AgronomicRuleEngine.evaluateIrrigationGuidance(

            cropName = "Cotton",

            weather = WeatherData(precipitationMm = 0.0),

            waterSource = "DRIP"

        )

        val evaluatedFertilizer = AgronomicRuleEngine.evaluateFertilizerGuidance(

            cropName = "Cotton",

            soilProfile = SoilProfile(ph = 7.8, nitrogen = 180.0, phosphorus = 15.0, potassium = 140.0),

            previousCrop = "Onion",

            farmSoilType = "Black Cotton"

        )

        val evaluatedCare = AgronomicRuleEngine.generateGrowthStageCareSchedule("Cotton")



        return CustomAgroPlan(

            id = "AGP-JAL-COTTON-2026",

            farmId = farmIdParam,

            farmName = "Khandesh Cotton Cluster",

            cropName = "Cotton",

            cropCategory = "Commercial Fiber",

            variety = "Bt Hybrid RCH-659",

            targetSeason = "Kharif 2025-2026",

            planVersion = 1,

            isLatest = true,

            suitabilitySummary = CropSuitabilitySummary(

                cropName = "Cotton (Bt RCH-659)",

                categoryName = "Fiber Cash Crop",

                compositeScore = 91,

                weatherScore = 18,

                climateScore = 23,

                soilScore = 24,

                rotationScore = 13,

                marketScore = 13,

                primaryRecommendationRationale = "High yield potential in vertisol deep black soil. Heavy stalk residue provides excellent feedstock for bio-energy briquetting.",

                agronomicAdvantages = listOf(

                    "Deep taproot system efficiently exploits moisture reserves in black clay",

                    "Massive residue yield (3.2 MT/acre) with high calorific pellet potential",

                    "Resistant to American bollworm with integrated BG-II traits"

                ),

                riskCaveats = listOf(

                    "Susceptible to pink bollworm during late season flush",

                    "Demands strict soil moisture regulation to avoid boll shedding"

                ),

                waterNeed = "650 - 750 mm across vegetative, square, and boll stages",

                expectedYieldRange = "12 - 15 Quintals Seed Cotton / Acre"

            ),

            economicsSnapshot = IndicativeEconomicsSummary(

                currency = "INR",

                acreage = 1.0,

                expectedYieldQuintals = 13.5,

                forecastModalRatePerQuintal = 7200.0,

                grossCropRevenue = 97200.0,

                residueBiomassTons = 3.2,

                residueValuePerTon = 2800.0,

                grossResidueRevenue = 8960.0,

                totalGrossRevenue = 106160.0,

                seedCost = 4200.0,

                fertilizerNutrientCost = 9800.0,

                cropProtectionCost = 5400.0,

                irrigationElectricityCost = 3200.0,

                laborHarvestCost = 14000.0,

                logisticsTransportCost = 3400.0,

                totalProductionCost = 40000.0,

                conservativeNetProfit = 52000.0,

                expectedNetProfit = 66160.0,

                optimisticNetProfit = 78000.0,

                costSourceNote = "Central Institute for Cotton Research (CICR) economic norms"

            ),

            irrigationGuidance = evaluatedIrrigation,

            fertilizerGuidance = evaluatedFertilizer,

            careSchedule = evaluatedCare,

            residuePlan = CropResiduePlan(

                residueName = "Dry Cotton Stalk Residue",

                estimatedBiomassTonsPerAcre = 3.2,

                targetMoisturePercentMax = 12.0,

                suggestedIndustrialApplication = "Industrial Biomass Briquettes & Bio-Gas Gasifiers",

                estimatedResidueValuePerTonInr = 2800.0,

                inSituManagementAlternative = "Shredding with tractor flail mower for in-situ soil mulch"

            ),

            activeAdaptationAlerts = if (adaptationNotice != null) listOf(adaptationNotice) else emptyList()

        )

    }



    private fun createSugarcanePlan(): CustomAgroPlan {
        val (evaluatedIrrigation, adaptationNotice) = AgronomicRuleEngine.evaluateIrrigationGuidance(
            cropName = "Sugarcane",
            weather = WeatherData(precipitationMm = 0.0),
            waterSource = "DRIP"
        )
        val evaluatedFertilizer = AgronomicRuleEngine.evaluateFertilizerGuidance(
            cropName = "Sugarcane",
            soilProfile = SoilProfile(ph = 7.6, nitrogen = 260.0, phosphorus = 22.0, potassium = 180.0),
            previousCrop = "Chickpea",
            farmSoilType = "Alluvial Loam"
        )
        val evaluatedCare = AgronomicRuleEngine.generateGrowthStageCareSchedule("Sugarcane")

        return CustomAgroPlan(
            id = "AGP-KPG-CANE-2026",
            farmId = farmIdParam,
            farmName = "Godavari Riverine Basin Parcel",
            cropName = "Sugarcane",
            cropCategory = "Cash Crop",
            variety = "Co 86032 (Nira)",
            targetSeason = "Suru / Pre-seasonal 2025-2026",
            planVersion = 1,
            isLatest = true,
            suitabilitySummary = CropSuitabilitySummary(
                cropName = "Sugarcane (Co 86032)",
                categoryName = "High Water Cash Crop",
                compositeScore = 88,
                weatherScore = 17,
                climateScore = 22,
                soilScore = 23,
                rotationScore = 13,
                marketScore = 13,
                primaryRecommendationRationale = "High biomass producing perennial crop suited to deep fertile loam with dependable canal or drip fertigation access.",
                agronomicAdvantages = listOf(
                    "Tremendous cane yield potential up to 55 MT/acre under paired-row drip",
                    "Massive trash/bagasse volume (4.8 MT/acre) with lucrative pellet mill off-take",
                    "High sugar recovery percentage with proven Co-86032 tissue culture clones"
                ),
                riskCaveats = listOf(
                    "Exceedingly high crop water requirement exceeding 1800 mm",
                    "Long field lock-in period of 12-14 months prevents rapid rotation"
                ),
                waterNeed = "1800 - 2200 mm across 4 major growth phases",
                expectedYieldRange = "45 - 55 Metric Tonnes Cane / Acre"
            ),
            economicsSnapshot = IndicativeEconomicsSummary(
                currency = "INR",
                acreage = 1.0,
                expectedYieldQuintals = 500.0,
                forecastModalRatePerQuintal = 340.0,
                grossCropRevenue = 170000.0,
                residueBiomassTons = 4.8,
                residueValuePerTon = 2400.0,
                grossResidueRevenue = 11520.0,
                totalGrossRevenue = 181520.0,
                seedCost = 16000.0,
                fertilizerNutrientCost = 18500.0,
                cropProtectionCost = 4200.0,
                irrigationElectricityCost = 7500.0,
                laborHarvestCost = 28000.0,
                logisticsTransportCost = 6500.0,
                totalProductionCost = 80700.0,
                conservativeNetProfit = 75000.0,
                expectedNetProfit = 100820.0,
                optimisticNetProfit = 125000.0,
                costSourceNote = "Vasantdada Sugar Institute (VSI) Pune economic recommendations"
            ),
            irrigationGuidance = evaluatedIrrigation,
            fertilizerGuidance = evaluatedFertilizer,
            careSchedule = evaluatedCare,
            residuePlan = CropResiduePlan(
                residueName = "Sugarcane Dry Trash & Tops Biomass",
                estimatedBiomassTonsPerAcre = 4.8,
                targetMoisturePercentMax = 15.0,
                suggestedIndustrialApplication = "Sugar Mill Bagasse Co-Gen Boilers & Cattle Fodder Pellets",
                estimatedResidueValuePerTonInr = 2400.0,
                inSituManagementAlternative = "Trash shredding into furrows + Trichoderma cellulolytic decomposer spray"
            ),
            activeAdaptationAlerts = if (adaptationNotice != null) listOf(adaptationNotice) else emptyList()
        )
    }

    private fun createSoybeanPlan(): CustomAgroPlan {
        val (evaluatedIrrigation, adaptationNotice) = AgronomicRuleEngine.evaluateIrrigationGuidance(
            cropName = "Soybean",
            weather = WeatherData(precipitationMm = 5.5),
            waterSource = "DRIP"
        )
        val evaluatedFertilizer = AgronomicRuleEngine.evaluateFertilizerGuidance(
            cropName = "Soybean",
            soilProfile = SoilProfile(ph = 7.1, nitrogen = 210.0, phosphorus = 18.0, potassium = 150.0),
            previousCrop = "Wheat",
            farmSoilType = "Black Cotton"
        )
        val evaluatedCare = AgronomicRuleEngine.generateGrowthStageCareSchedule("Soybean")

        return CustomAgroPlan(
            id = "AGP-NSK-SOY-2026",
            farmId = farmIdParam,
            farmName = "Sinnar Organic Parcel 01",
            cropName = "Soybean",
            cropCategory = "Oilseed / Legume",
            variety = "JS 335 / MACS 1407",
            targetSeason = "Kharif 2026",
            planVersion = 1,
            isLatest = true,
            suitabilitySummary = CropSuitabilitySummary(
                cropName = "Soybean (JS 335)",
                categoryName = "Pulse / Oilseed Rotation",
                compositeScore = 93,
                weatherScore = 19,
                climateScore = 23,
                soilScore = 24,
                rotationScore = 14,
                marketScore = 13,
                primaryRecommendationRationale = "Exceptional nitrogen-fixing rotational pulse. Enhances soil nitrogen reserves for the succeeding Rabi wheat crop.",
                agronomicAdvantages = listOf(
                    "Fixes 35-45 kg biological atmospheric nitrogen per acre in root nodules",
                    "Short duration crop of 90-95 days allowing prompt Rabi land preparation",
                    "Strong oil-mill extraction demand with firm APMC floor prices"
                ),
                riskCaveats = listOf(
                    "Germination severely hampered if waterlogging occurs post-sowing",
                    "Requires prompt pod-borer scouting at pre-pod fill phase"
                ),
                waterNeed = "450 - 500 mm primarily fulfilled by South-West monsoon",
                expectedYieldRange = "9 - 12 Quintals / Acre"
            ),
            economicsSnapshot = IndicativeEconomicsSummary(
                currency = "INR",
                acreage = 1.0,
                expectedYieldQuintals = 10.5,
                forecastModalRatePerQuintal = 4600.0,
                grossCropRevenue = 48300.0,
                residueBiomassTons = 1.8,
                residueValuePerTon = 2500.0,
                grossResidueRevenue = 4500.0,
                totalGrossRevenue = 52800.0,
                seedCost = 3500.0,
                fertilizerNutrientCost = 3800.0,
                cropProtectionCost = 2800.0,
                irrigationElectricityCost = 1200.0,
                laborHarvestCost = 7500.0,
                logisticsTransportCost = 1800.0,
                totalProductionCost = 20600.0,
                conservativeNetProfit = 24000.0,
                expectedNetProfit = 32200.0,
                optimisticNetProfit = 39000.0,
                costSourceNote = "ICAR - Indian Institute of Soybean Research (IISR) Indore benchmarks"
            ),
            irrigationGuidance = evaluatedIrrigation,
            fertilizerGuidance = evaluatedFertilizer,
            careSchedule = evaluatedCare,
            residuePlan = CropResiduePlan(
                residueName = "Dry Soybean Husk & Pod Shells Residue",
                estimatedBiomassTonsPerAcre = 1.8,
                targetMoisturePercentMax = 13.0,
                suggestedIndustrialApplication = "High-Protein Animal Feed Pelleting & Industrial Boiler Fuel",
                estimatedResidueValuePerTonInr = 2500.0,
                inSituManagementAlternative = "In-situ mulching using zero-till seed drill for Rabi wheat"
            ),
            activeAdaptationAlerts = if (adaptationNotice != null) listOf(adaptationNotice) else emptyList()
        )
    }
}

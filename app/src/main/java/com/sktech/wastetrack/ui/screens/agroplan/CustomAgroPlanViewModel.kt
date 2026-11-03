package com.sktech.wastetrack.ui.screens.agroplan

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
            irrigationGuidance = IrrigationPlanGuidance(
                baseMethod = "Precision Drip / Micro-Sprinkler",
                frequencyHours = "Every 48 hours for 2.5 hours",
                preferredTimeOfDay = "Early Morning (6:00 AM - 8:30 AM)",
                criticalWaterNeedStages = listOf(
                    "Crown Root Initiation (20-25 DAS)",
                    "Tillering to Jointing (40-45 DAS)",
                    "Flowering & Booting (65-70 DAS)",
                    "Milking & Dough Filling (85-90 DAS)"
                ),
                waterSavingTips = listOf(
                    "Maintain soil mulch with crop residues to curtail surface evaporation",
                    "Schedule water release via tensiometer or soil moisture sensor"
                ),
                rainHaltActive = true,
                rainAdjustmentNote = "HOLD IRRIGATION: Rainfall probability exceeds 60%. Natural soil moisture adequate."
            ),
            fertilizerGuidance = FertilizerPlanGuidance(
                soilSourceType = "SOIL_TEST_LAB",
                soilSummaryText = "Medium Black Soil (pH 7.2, Organic Carbon 0.62%, N Medium, P High, K High)",
                basalApplicationDose = "DAP 50 kg + MOP 25 kg + Zinc Sulphate 10 kg per acre",
                vegetativeTopDressDose = "Urea 35 kg / acre at 21 days after sowing (first irrigation)",
                floweringFertigationDose = "19:19:19 water-soluble NPK @ 3 kg/acre via fertigation at boot stage",
                micronutrientPrescription = "Foliar spray of 0.5% Ferrous Sulphate + Citric Acid at tillering",
                organicCompostDose = "Well-rotted Farmyard Manure (FYM) 4 Tons / acre or Vermicompost 1.5 Tons",
                previousCropCarryOverCredit = "Nitrogen credit +15 kg/acre carried over from previous Soybean nodule fixation",
                soilTestRecommendationNotice = "Soil test valid up to March 2026. Nitrogen booster recommended."
            ),
            careSchedule = listOf(
                GrowthStageScheduleStep(
                    stageOrder = 1,
                    stageName = "Sowing & Crown Root Initiation",
                    windowDescription = "Day 0 - 25",
                    approximateDurationDays = 25,
                    keyObjectives = "Ensure optimal seed germination and crown root establishment.",
                    actions = listOf(
                        ScheduledCareAction("act_001", "Seed Treatment with Trichoderma", "SOIL_TREATMENT", "Treat seeds with Trichoderma viride @ 4g/kg seed for root rot prevention.", true, true),
                        ScheduledCareAction("act_002", "Basal Fertilizer Placement", "FERTILIZER", "Apply DAP 50 kg + MOP 25 kg per acre below seed depth during drilling.", true, true),
                        ScheduledCareAction("act_003", "First Light Irrigation (CRI)", "IRRIGATION", "Provide first irrigation at 21 DAS. Avoid water stagnation.", true, false)
                    ),
                    stageWarnings = listOf("Avoid deep sowing beyond 5 cm to ensure uniform seedling emergence.")
                ),
                GrowthStageScheduleStep(
                    stageOrder = 2,
                    stageName = "Tillering & Jointing",
                    windowDescription = "Day 26 - 55",
                    approximateDurationDays = 30,
                    keyObjectives = "Maximize productive tillers and strong shoot elongation.",
                    actions = listOf(
                        ScheduledCareAction("act_004", "Top Dress First Urea Dose", "FERTILIZER", "Broadcast Urea 35 kg/acre after light irrigation.", true, false),
                        ScheduledCareAction("act_005", "Broadleaf Weed Management", "WEEDING_CULTURE", "Spray 2,4-D amine salt @ 400ml/acre at 30-35 DAS if weeds emerge.", false, false),
                        ScheduledCareAction("act_006", "Aphid & Rust Surveillance", "IPM_PROTECTION", "Inspect flag leaves for yellow rust pustules or aphid clusters.", true, false)
                    ),
                    stageWarnings = listOf("Suspending irrigation during windy periods prevents early crop lodging.")
                ),
                GrowthStageScheduleStep(
                    stageOrder = 3,
                    stageName = "Flowering & Grain Filling",
                    windowDescription = "Day 56 - 95",
                    approximateDurationDays = 40,
                    keyObjectives = "Prevent moisture deficit during boot and milk stages.",
                    actions = listOf(
                        ScheduledCareAction("act_007", "Soluble Potassium Foliar Spray", "FERTILIZER", "Spray 0:0:50 @ 5g/liter during dough stage for grain boldness.", false, false),
                        ScheduledCareAction("act_008", "Critical Milk Stage Irrigation", "IRRIGATION", "Ensure steady soil moisture during dough formation.", true, false)
                    ),
                    stageWarnings = listOf("Terminal heat during milking can lead to shriveled grain; maintain soil moisture.")
                ),
                GrowthStageScheduleStep(
                    stageOrder = 4,
                    stageName = "Maturity, Harvest & Residue Baling",
                    windowDescription = "Day 96 - 125",
                    approximateDurationDays = 30,
                    keyObjectives = "Harvest grain at 12% moisture and bale straw residue for zero burning.",
                    actions = listOf(
                        ScheduledCareAction("act_009", "Pre-Harvest Field Drying", "HARVEST_RESIDUE", "Stop irrigation 10 days prior to combine harvesting.", true, false),
                        ScheduledCareAction("act_010", "Straw Reaper & Square Baling", "HARVEST_RESIDUE", "Bale wheat straw immediately into 25kg bundles at < 14% moisture.", true, false),
                        ScheduledCareAction("act_011", "Publish Stubble Listing on AgriStock", "HARVEST_RESIDUE", "Upload consignment manifest for thermal plant buyback at INR 3,000/MT.", true, false)
                    ),
                    stageWarnings = listOf("Zero field burning policy mandatory under MPCB regulations.")
                )
            ),
            residuePlan = CropResiduePlan(
                residueName = "Dry Wheat Straw / Stubble Residue",
                estimatedBiomassTonsPerAcre = 2.4,
                targetMoisturePercentMax = 14.0,
                suggestedIndustrialApplication = "Thermal Co-Firing Power Generation & Agro-Paper Pulp Mills",
                estimatedResidueValuePerTonInr = 3000.0,
                inSituManagementAlternative = "Incorporation with Super Seeder / Rotavator + PUSA Bio-Decomposer"
            ),
            activeAdaptationAlerts = listOf(
                WeatherAdaptationNotice(
                    noticeId = "NT-RAIN-01",
                    urgencyLevel = "ADVISORY",
                    title = "Rainfall Advisory in Sinnar Cluster",
                    headline = "65% precipitation probability forecast over the next 24 hours.",
                    recommendedFarmerAction = "Suspend scheduled drip irrigation. Natural precipitation will replenish root zone moisture."
                )
            )
        )
    }

    private fun createCottonPlan(): CustomAgroPlan {
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
                cropName = "Cotton (Bt Hybrid)",
                categoryName = "Fiber Crops",
                compositeScore = 91,
                weatherScore = 18,
                climateScore = 23,
                soilScore = 24,
                rotationScore = 13,
                marketScore = 13,
                primaryRecommendationRationale = "Deep black soil ideal for taproot expansion and drought tolerance. Exceptional woody stalk residue volume for bio-briquettes.",
                agronomicAdvantages = listOf(
                    "High calorific value stalk residue (3,980 kcal/kg)",
                    "Deep root penetration resists moisture stress",
                    "Strong local ginning and briquetting cluster demand"
                ),
                riskCaveats = listOf("Vulnerable to pink bollworm; requires pheromone trap monitoring."),
                waterNeed = "650 - 750 mm across season",
                expectedYieldRange = "10 - 14 Quintals / Acre"
            ),
            economicsSnapshot = IndicativeEconomicsSummary(
                currency = "INR",
                acreage = 1.0,
                expectedYieldQuintals = 12.0,
                forecastModalRatePerQuintal = 7100.0,
                grossCropRevenue = 85200.0,
                residueBiomassTons = 2.8,
                residueValuePerTon = 2800.0,
                grossResidueRevenue = 7840.0,
                totalGrossRevenue = 93040.0,
                seedCost = 4500.0,
                fertilizerNutrientCost = 8200.0,
                cropProtectionCost = 5400.0,
                irrigationElectricityCost = 3500.0,
                laborHarvestCost = 14200.0,
                logisticsTransportCost = 2800.0,
                totalProductionCost = 38600.0,
                conservativeNetProfit = 44000.0,
                expectedNetProfit = 54440.0,
                optimisticNetProfit = 65000.0,
                costSourceNote = "Based on CICR Nagpur Cotton Research Institute norms"
            ),
            irrigationGuidance = IrrigationPlanGuidance(
                baseMethod = "Alternate Furrow / Precision Drip",
                frequencyHours = "Every 72 hours for 3 hours",
                preferredTimeOfDay = "Morning (7:00 AM - 10:00 AM)",
                criticalWaterNeedStages = listOf("Square Formation", "Flowering & Boll Development"),
                waterSavingTips = listOf("Intercropping with green gram reduces weed growth and conserves soil water."),
                rainHaltActive = false
            ),
            fertilizerGuidance = FertilizerPlanGuidance(
                soilSourceType = "SOIL_TEST_LAB",
                soilSummaryText = "Deep Black Clay Soil (pH 7.8, High Potassium, Moderate Phosphorus)",
                basalApplicationDose = "NPK 10:26:26 @ 50 kg + Magnesium Sulphate 10 kg per acre",
                vegetativeTopDressDose = "Urea 30 kg + Ammonium Sulphate 15 kg split at 30 & 60 DAS",
                floweringFertigationDose = "Potassium Nitrate (13:0:45) @ 5 kg/acre during boll filling",
                micronutrientPrescription = "Boron 0.2% spray at squaring stage to prevent flower drop",
                organicCompostDose = "Castor Cake 250 kg + FYM 3 Tons per acre",
                previousCropCarryOverCredit = "No carryover; full basal application required",
                soilTestRecommendationNotice = "Zinc deficiency corrected during land prep."
            ),
            careSchedule = listOf(
                GrowthStageScheduleStep(
                    stageOrder = 1,
                    stageName = "Sowing to Squaring",
                    windowDescription = "Day 0 - 45",
                    approximateDurationDays = 45,
                    keyObjectives = "Uniform stand establishment and early sucking pest control.",
                    actions = listOf(
                        ScheduledCareAction("cact_001", "Seed Treatment with Imidacloprid", "SOIL_TREATMENT", "Protect young seedlings against thrips and jassids.", true, true),
                        ScheduledCareAction("cact_002", "Pheromone Trap Setup", "IPM_PROTECTION", "Install 5 PBR traps per acre for pink bollworm monitoring.", true, false)
                    ),
                    stageWarnings = listOf("Avoid waterlogging during initial 20 days.")
                ),
                GrowthStageScheduleStep(
                    stageOrder = 2,
                    stageName = "Boll Development & Picking",
                    windowDescription = "Day 46 - 150",
                    approximateDurationDays = 105,
                    keyObjectives = "Ensure clean picking and shredding of dry stalks.",
                    actions = listOf(
                        ScheduledCareAction("cact_003", "Split Urea Top Dressing", "FERTILIZER", "Apply second urea dose at peak boll formation.", true, false),
                        ScheduledCareAction("cact_004", "Tractor-Mounted Stalk Shredding", "HARVEST_RESIDUE", "Chop dry stalks with flail mower for pellet cluster delivery.", true, false)
                    ),
                    stageWarnings = listOf("Do not graze livestock on Bt cotton residue immediately after picking.")
                )
            ),
            residuePlan = CropResiduePlan(
                residueName = "Dry Cotton Stalk Biomass Residue",
                estimatedBiomassTonsPerAcre = 2.8,
                targetMoisturePercentMax = 12.0,
                suggestedIndustrialApplication = "Industrial Bio-Pellet Plants & Gasifier Power Generation",
                estimatedResidueValuePerTonInr = 2800.0,
                inSituManagementAlternative = "Tractor Shredding into soil followed by Trichoderma spray"
            )
        )
    }

    private fun createSugarcanePlan(): CustomAgroPlan {
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
                categoryName = "Sugar & Energy Crops",
                compositeScore = 88,
                weatherScore = 17,
                climateScore = 23,
                soilScore = 23,
                rotationScore = 12,
                marketScore = 13,
                primaryRecommendationRationale = "High biomass producing perennial crop with huge commercial bagasse and sugarcane trash yields for cogeneration power.",
                agronomicAdvantages = listOf(
                    "High sugar recovery and heavy cane tonnage",
                    "Trash residue (4.5 MT/acre) ideal for 2G bio-ethanol refinery"
                ),
                riskCaveats = listOf("Heavy water requirement; requires drip fertigation."),
                waterNeed = "1800 - 2200 mm across 12 months",
                expectedYieldRange = "45 - 55 Tons / Acre"
            ),
            economicsSnapshot = IndicativeEconomicsSummary(
                currency = "INR",
                acreage = 1.0,
                expectedYieldQuintals = 500.0,
                forecastModalRatePerQuintal = 340.0,
                grossCropRevenue = 170000.0,
                residueBiomassTons = 4.5,
                residueValuePerTon = 2200.0,
                grossResidueRevenue = 9900.0,
                totalGrossRevenue = 179900.0,
                seedCost = 14000.0,
                fertilizerNutrientCost = 22000.0,
                cropProtectionCost = 6500.0,
                irrigationElectricityCost = 11000.0,
                laborHarvestCost = 32000.0,
                logisticsTransportCost = 9000.0,
                totalProductionCost = 94500.0,
                conservativeNetProfit = 65000.0,
                expectedNetProfit = 85400.0,
                optimisticNetProfit = 105000.0,
                costSourceNote = "Based on VSI Pune Vasantdada Sugar Institute norms"
            ),
            irrigationGuidance = IrrigationPlanGuidance(
                baseMethod = "Sub-surface Inline Drip Irrigation",
                frequencyHours = "Every 48 hours for 4 hours",
                preferredTimeOfDay = "Night / Early Morning",
                criticalWaterNeedStages = listOf("Formative Stage", "Grand Growth Stage"),
                waterSavingTips = listOf("Trash mulching conserves up to 35% soil moisture."),
                rainHaltActive = false
            ),
            fertilizerGuidance = FertilizerPlanGuidance(
                soilSourceType = "SOIL_TEST_LAB",
                soilSummaryText = "Medium Black Soil (pH 7.5, High Organic Matter)",
                basalApplicationDose = "DAP 100 kg + MOP 50 kg + Urea 50 kg at planting",
                vegetativeTopDressDose = "Urea 75 kg + Ammonium Sulphate 50 kg at earthing up (120 DAP)",
                floweringFertigationDose = "Weekly drip fertigation with 12:61:0 and 0:0:50",
                micronutrientPrescription = "Ferrous + Zinc chelated foliar spray",
                organicCompostDose = "Pressmud 5 Tons or Compost 8 Tons per acre",
                previousCropCarryOverCredit = "Heavy feeder; follow with green gram rotation",
                soilTestRecommendationNotice = "Sulfur replenishment recommended."
            ),
            careSchedule = listOf(
                GrowthStageScheduleStep(
                    stageOrder = 1,
                    stageName = "Germination & Tillering",
                    windowDescription = "Day 0 - 120",
                    approximateDurationDays = 120,
                    keyObjectives = "Establish uniform ratoon or planted crop with deep root network.",
                    actions = listOf(
                        ScheduledCareAction("sact_001", "Sett Treatment in Fungicide Solution", "SOIL_TREATMENT", "Dip two-eye setts in carbendazim solution for 15 minutes.", true, true),
                        ScheduledCareAction("sact_002", "Earthing Up and Fertilizer Dressing", "FERTILIZER", "Earthing up at 120 days to prevent lodging.", true, false)
                    ),
                    stageWarnings = listOf("Control early shoot borer using trichocards.")
                ),
                GrowthStageScheduleStep(
                    stageOrder = 2,
                    stageName = "Harvest & Trash Valorization",
                    windowDescription = "Day 300 - 365",
                    approximateDurationDays = 65,
                    keyObjectives = "Cane cutting and zero burning trash baling for 2G bio-ethanol.",
                    actions = listOf(
                        ScheduledCareAction("sact_003", "Sugarcane Trash Raking & Baling", "HARVEST_RESIDUE", "Rake dry trash into rows and bale into 30kg bundles.", true, false),
                        ScheduledCareAction("sact_004", "Dispatch Trash to Kopargaon Refinery", "HARVEST_RESIDUE", "Ship trash manifest to Godavari Green Biofuels.", true, false)
                    ),
                    stageWarnings = listOf("Trash burning strictly banned by MPCB.")
                )
            ),
            residuePlan = CropResiduePlan(
                residueName = "Sugarcane Dry Trash & Bagasse Residue",
                estimatedBiomassTonsPerAcre = 4.5,
                targetMoisturePercentMax = 15.0,
                suggestedIndustrialApplication = "2G Bio-Ethanol Refineries & Cogeneration Boilers",
                estimatedResidueValuePerTonInr = 2200.0,
                inSituManagementAlternative = "In-situ trash mulching with ratoon management device"
            )
        )
    }

    private fun createSoybeanPlan(): CustomAgroPlan {
        return CustomAgroPlan(
            id = "AGP-NSK-SOY-2026",
            farmId = farmIdParam,
            farmName = "Dindori Agri Collective Parcel",
            cropName = "Soybean",
            cropCategory = "Oilseed / Legume",
            variety = "JS 335 / JS 9305",
            targetSeason = "Kharif 2025-2026",
            planVersion = 1,
            isLatest = true,
            suitabilitySummary = CropSuitabilitySummary(
                cropName = "Soybean (JS 335)",
                categoryName = "Oilseeds & Legumes",
                compositeScore = 93,
                weatherScore = 19,
                climateScore = 24,
                soilScore = 23,
                rotationScore = 14,
                marketScore = 13,
                primaryRecommendationRationale = "Excellent rainfed legume fixing up to 40 kg atmospheric nitrogen per acre. Provides high-protein straw residue for livestock & bio-pellets.",
                agronomicAdvantages = listOf(
                    "Fixes natural nitrogen in soil nodules",
                    "Short duration (90-95 days) allowing timely Rabi wheat planting",
                    "High oil extraction meal and stalk demand"
                ),
                riskCaveats = listOf("Susceptible to waterlogging during heavy downpours."),
                waterNeed = "450 - 500 mm across monsoon",
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
                conservativeNetProfit = 26000.0,
                expectedNetProfit = 32200.0,
                optimisticNetProfit = 38500.0,
                costSourceNote = "Based on ICAR Indian Institute of Soybean Research norms"
            ),
            irrigationGuidance = IrrigationPlanGuidance(
                baseMethod = "Broad Bed Furrow (BBF) Rainfed Drainage",
                frequencyHours = "As needed during monsoon dry spells",
                preferredTimeOfDay = "Late Afternoon",
                criticalWaterNeedStages = listOf("Pod Initiation (45-50 DAS)", "Seed Filling (65-70 DAS)"),
                waterSavingTips = listOf("Ensure drainage furrows are open to avoid root rot."),
                rainHaltActive = true
            ),
            fertilizerGuidance = FertilizerPlanGuidance(
                soilSourceType = "SOIL_TEST_LAB",
                soilSummaryText = "Medium Black Loam Soil (pH 7.0, Good Nodule Formation Potential)",
                basalApplicationDose = "SSP 150 kg + MOP 30 kg + Zinc Sulphate 8 kg per acre",
                vegetativeTopDressDose = "Rhizobium + PSB seed inoculation provides bulk of Nitrogen",
                floweringFertigationDose = "Foliar spray of 19:19:19 @ 1 kg/acre at flowering",
                micronutrientPrescription = "Boron 0.1% at pod formation to prevent pod dropping",
                organicCompostDose = "Compost 2.5 Tons per acre",
                previousCropCarryOverCredit = "Leaves +15 to +20 kg N credit for subsequent wheat crop",
                soilTestRecommendationNotice = "Phosphate rich SSP ensures high oil content."
            ),
            careSchedule = listOf(
                GrowthStageScheduleStep(
                    stageOrder = 1,
                    stageName = "Sowing to Pod Setting",
                    windowDescription = "Day 0 - 55",
                    approximateDurationDays = 55,
                    keyObjectives = "Healthy vegetative canopy with active root nodulation.",
                    actions = listOf(
                        ScheduledCareAction("soact_001", "Rhizobium Culture Seed Inoculation", "SOIL_TREATMENT", "Inoculate seeds with Rhizobium japonicum @ 250g per 10kg seed.", true, true),
                        ScheduledCareAction("soact_002", "Pre-emergence Herbicide Spray", "WEEDING_CULTURE", "Apply Diclosulam within 48 hours of sowing.", true, false)
                    ),
                    stageWarnings = listOf("Avoid sowing in dry soil; ensure minimum 75mm rain received.")
                ),
                GrowthStageScheduleStep(
                    stageOrder = 2,
                    stageName = "Harvest & Straw Collection",
                    windowDescription = "Day 56 - 95",
                    approximateDurationDays = 40,
                    keyObjectives = "Timely pod harvest before pod shattering and straw recovery.",
                    actions = listOf(
                        ScheduledCareAction("soact_003", "Harvest at 14% Pod Moisture", "HARVEST_RESIDUE", "Reap when 95% of leaves have shed and pods turn golden brown.", true, false),
                        ScheduledCareAction("soact_004", "Bale Soybean Husk & Stalks", "HARVEST_RESIDUE", "Collect threshing waste for bio-briquette supply.", true, false)
                    ),
                    stageWarnings = listOf("Delayed harvest causes extensive pod shattering in dry weather.")
                )
            ),
            residuePlan = CropResiduePlan(
                residueName = "Soybean Chaff, Husk & Stalk Residue",
                estimatedBiomassTonsPerAcre = 1.8,
                targetMoisturePercentMax = 12.0,
                suggestedIndustrialApplication = "Bio-Pellet Briquetting Clusters & Fodder Mix",
                estimatedResidueValuePerTonInr = 2500.0,
                inSituManagementAlternative = "Shredding and incorporating as green organic manure"
            )
        )
    }
}


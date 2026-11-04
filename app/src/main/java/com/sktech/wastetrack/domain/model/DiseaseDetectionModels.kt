package com.sktech.wastetrack.domain.model

enum class PathogenSeverity(val displayName: String) {
    HEALTHY("Healthy Crop"),
    LOW("Low Severity (1% to 15%)"),
    MODERATE("Moderate Infection (16% to 40%)"),
    HIGH("Severe Outbreak (41% to 70%)"),
    CRITICAL("Critical Foliar Blight (>70%)")
}

enum class ResidueEndUseRecommendation(
    val title: String,
    val description: String,
    val isRecommended: Boolean
) {
    BIOCHAR_PYROLYSIS(
        "Biochar Pyrolysis Kiln",
        "Ideal for low-moisture woody or fibrous stubble (<20% moisture). Thermally converts pathogen-infected stalks into stable carbon.",
        true
    ),
    BRIQUETTING_PELLETS(
        "Agro-Briquette Pelleting",
        "Suitable for clean cured straw with <15% moisture. High mechanical compaction and calorific density.",
        true
    ),
    BIO_CNG_METHANATION(
        "Bio-CNG Anaerobic Digestion",
        "Recommended for high-moisture green foliage and succulent residues (>40% moisture).",
        false
    ),
    COMPOSTING_MULCH(
        "Aerobic Farm Composting",
        "Safe only if no high-risk fungal sclerotia or viral pathogens are detected.",
        false
    ),
    BOILER_CO_FIRING(
        "Industrial Boiler Co-Firing",
        "Industrial thermal off-take at power plants and sugar mills (>3,200 kcal/kg).",
        true
    ),
    QUARANTINE_ISOLATION(
        "Pathogen Quarantine & Safe Destruction",
        "Severe bacterial or viral pathogen detected. Isolate immediately to prevent field propagation.",
        false
    )
}

data class StubbleDiseaseDiagnosis(
    val cropName: String,
    val diseaseName: String,
    val isHealthy: Boolean,
    val confidence: Float,
    val severityPercentage: Int,
    val severityLevel: PathogenSeverity,
    val organicRemedy: String,
    val chemicalTreatment: String,
    val preventativeSteps: List<String>,
    val endUseRecommendation: ResidueEndUseRecommendation,
    val modelSource: String = "AgriEdge Neural TFLite Engine"
)

data class BiomassDegradationScore(
    val estimatedMoisturePercent: Float,
    val biomassDryMatterPercent: Float,
    val qualityGrade: String,
    val qualityScore: Int,
    val degradationStatus: String,
    val rotRiskPercentage: Int,
    val fibrousDensityScore: Float,
    val dominantColorHex: String,
    val contaminationNotice: String? = null
)

data class DiagnosticSamplePreset(
    val id: String,
    val cropName: String,
    val conditionName: String,
    val sampleDescription: String,
    val defaultMoisture: Float,
    val diagnosis: StubbleDiseaseDiagnosis
)

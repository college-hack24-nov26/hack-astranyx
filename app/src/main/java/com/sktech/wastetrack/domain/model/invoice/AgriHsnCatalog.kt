package com.sktech.wastetrack.domain.model.invoice

data class AgriHsnItem(
    val hsnCode: String,
    val commodityName: String,
    val description: String,
    val gstRatePercent: Double = 5.0,
    val benchmarkRatePerMt: Double,
    val stubbleEmissionFactorKgCo2PerKg: Double = 1.46
)

object AgriHsnCatalog {
    val items: List<AgriHsnItem> = listOf(
        AgriHsnItem(
            hsnCode = "1213",
            commodityName = "Cereal Straw & Stubble",
            description = "Wheat / Paddy straw bales, unprepared or compressed (HSN 1213)",
            gstRatePercent = 5.0,
            benchmarkRatePerMt = 2200.0,
            stubbleEmissionFactorKgCo2PerKg = 1.46
        ),
        AgriHsnItem(
            hsnCode = "1404",
            commodityName = "Cotton Stalks & Agro-Fibers",
            description = "Cotton stalk biomass for particle board & bio-char pyrolysis (HSN 1404)",
            gstRatePercent = 5.0,
            benchmarkRatePerMt = 2800.0,
            stubbleEmissionFactorKgCo2PerKg = 1.46
        ),
        AgriHsnItem(
            hsnCode = "2308",
            commodityName = "Sugarcane Bagasse & Trash",
            description = "Residues for biomass boiler cogeneration & animal fodder (HSN 2308)",
            gstRatePercent = 5.0,
            benchmarkRatePerMt = 1900.0,
            stubbleEmissionFactorKgCo2PerKg = 1.46
        ),
        AgriHsnItem(
            hsnCode = "4401",
            commodityName = "Biomass Pellets & Briquettes",
            description = "Compacted agricultural stubble fuel pellets for power plants (HSN 4401)",
            gstRatePercent = 5.0,
            benchmarkRatePerMt = 4500.0,
            stubbleEmissionFactorKgCo2PerKg = 1.65
        ),
        AgriHsnItem(
            hsnCode = "3101",
            commodityName = "Organic Compost & Green Manure",
            description = "Bio-fertilizer, fermented green biomass & vermicompost (HSN 3101)",
            gstRatePercent = 5.0,
            benchmarkRatePerMt = 3500.0,
            stubbleEmissionFactorKgCo2PerKg = 1.20
        )
    )

    fun findByHsn(hsn: String): AgriHsnItem? = items.find { it.hsnCode == hsn }
}

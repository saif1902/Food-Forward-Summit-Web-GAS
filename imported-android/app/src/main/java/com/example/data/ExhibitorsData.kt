package com.example.data

data class Exhibitor(
    val id: String,
    val name: String,
    val focus: String,
    val track: String,
    val description: String,
    val boothLocation: String,
    val website: String,
    val contactEmail: String,
    val tier: String, // Platinum, Gold, Silver
    val logoAsset: String // Simulation or description of logo
)

object ExhibitorsData {
    val list = listOf(
        Exhibitor(
            id = "ex_biocult",
            name = "BioCult Tech",
            focus = "Lab-grown alternative proteins and bioreactor scaling systems.",
            track = "Tech & Innovation",
            description = "BioCult Tech is pioneering cellular agriculture by designing industrial-scale modular bioreactors that make cellular beef, poultry, and fish alternatives commercially viable.",
            boothLocation = "Booth A-12",
            website = "https://biocult.tech",
            contactEmail = "match@biocult.tech",
            tier = "Platinum",
            logoAsset = "🔬"
        ),
        Exhibitor(
            id = "ex_ecopack",
            name = "EcoPack Solutions",
            focus = "Zero-waste seaweed-based packaging materials.",
            track = "Sustainability & Packaging",
            description = "Developing bio-degradable polymer wraps using renewable marine algae that decay harmlessly in any domestic composting bin within 6 weeks.",
            boothLocation = "Booth B-05",
            website = "https://ecopacksolutions.org",
            contactEmail = "partner@ecopack.org",
            tier = "Platinum",
            logoAsset = "🌿"
        ),
        Exhibitor(
            id = "ex_agridrone",
            name = "AgriDrone Logistics",
            focus = "Autonomous precision drone mapping and delivery.",
            track = "Supply Chain & Automation",
            description = "Aero-agricultural flight vectors combined with AI multispectral sensors to scan nutrients and deliver water or localized organic compounds with zero waste.",
            boothLocation = "Booth C-08",
            website = "https://agridronelogix.com",
            contactEmail = "fleet@agridrone.com",
            tier = "Gold",
            logoAsset = "🛸"
        ),
        Exhibitor(
            id = "ex_verdevertical",
            name = "VerdeVerticals",
            focus = "Modular smart vertical hydroponic setups.",
            track = "Supply Chain & Automation",
            description = "Space-maximizing agricultural assemblies for metropolitan cores. Employs circular water recycling tech that saves up to 95% of fresh water.",
            boothLocation = "Booth C-14",
            website = "https://verdevertical.io",
            contactEmail = "grow@verdevertical.io",
            tier = "Gold",
            logoAsset = "🌱"
        ),
        Exhibitor(
            id = "ex_freezefresh",
            name = "FreezeFresh Logix",
            focus = "Phase-change nitrogen cold chain monitoring.",
            track = "Supply Chain & Automation",
            description = "We design advanced refrigerated cargo storage using passive phase change elements, ensuring consistent sub-zero vaccine or frozen protein shipping without continuous grid-power drawing.",
            boothLocation = "Booth D-01",
            website = "https://freezefreshlogix.com",
            contactEmail = "coldchain@freezefresh.com",
            tier = "Silver",
            logoAsset = "❄️"
        ),
        Exhibitor(
            id = "ex_mycelium",
            name = "Mycelium Foods",
            focus = "Whole-tissue meat replacements from fungi spores.",
            track = "Tech & Innovation",
            description = "Mycelium Foods brews dense, textured whole cuts like prime steaks and fillets entirely from fungi filaments root-systems, avoiding synthetic processes.",
            boothLocation = "Booth A-20",
            website = "https://myceliumfoods.com",
            contactEmail = "chef@myceliumfoods.com",
            tier = "Platinum",
            logoAsset = "🍄"
        ),
        Exhibitor(
            id = "ex_aqualoop",
            name = "AquaLoop Marine",
            focus = "On-shore solar-powered marine aquaculture systems.",
            track = "Sustainability & Packaging",
            description = "Providing high-efficiency land-based seawater recycling containment systems for prawns and organic kelp, with full thermal and nutrient salvage.",
            boothLocation = "Booth B-19",
            website = "https://aqualoopmarine.com",
            contactEmail = "info@aqualoopmarine.com",
            tier = "Silver",
            logoAsset = "🌊"
        ),
        Exhibitor(
            id = "ex_chocotrace",
            name = "ChocoTrace Block",
            focus = "Decentralized forest compliance tracking software.",
            track = "Consumer & Regulatory",
            description = "Blockchain consensus verification ledger that aggregates geolocation scans from cocoa farmers, proving absolute compliance with anti-deforestation standards.",
            boothLocation = "Booth E-04",
            website = "https://chocotrace.io",
            contactEmail = "compliance@chocotrace.io",
            tier = "Gold",
            logoAsset = "🍫"
        )
    )
}

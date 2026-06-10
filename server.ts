import express from "express";
import path from "path";
import { createServer as createViteServer } from "vite";
import { GoogleGenAI } from "@google/genai";
import dotenv from "dotenv";
import { 
  loadDatabase, 
  getExhibitors, 
  getSpeakers, 
  addOrUpdateExhibitor, 
  addOrUpdateSpeaker 
} from "./server-db";

dotenv.config();

const app = express();
const PORT = 3000;

app.use(express.json());

// List of exhibitors for local fallback matchmaking logic (merged with dynamic database later)
const staticExhibitors = [
  {
    id: "ex_biocult",
    name: "BioCult Tech",
    focus: "Lab-grown alternative proteins and bioreactor scaling systems.",
    track: "Tech & Innovation",
    description: "BioCult Tech is pioneering cellular agriculture by designing industrial-scale modular bioreactors that make cellular beef, poultry, and fish alternatives commercially viable.",
    tier: "Platinum"
  },
  {
    id: "ex_ecopack",
    name: "EcoPack Solutions",
    focus: "Zero-waste seaweed-based packaging materials.",
    track: "Sustainability & Packaging",
    description: "Developing bio-degradable polymer wraps using renewable marine algae that decay harmlessly in any domestic composting bin within 6 weeks.",
    tier: "Platinum"
  },
  {
    id: "ex_agridrone",
    name: "AgriDrone Logistics",
    focus: "Autonomous precision drone mapping and delivery.",
    track: "Supply Chain & Automation",
    description: "Aero-agricultural flight vectors combined with AI multispectral sensors to scan nutrients and deliver water or localized organic compounds with zero waste.",
    tier: "Gold"
  },
  {
    id: "ex_verdevertical",
    name: "VerdeVerticals",
    focus: "Modular smart vertical hydroponic setups.",
    track: "Supply Chain & Automation",
    description: "Space-maximizing agricultural assemblies for metropolitan cores. Employs circular water recycling tech that saves up to 95% of fresh water.",
    tier: "Gold"
  },
  {
    id: "ex_freezefresh",
    name: "FreezeFresh Logix",
    focus: "Phase-change nitrogen cold chain monitoring.",
    track: "Supply Chain & Automation",
    description: "We design advanced refrigerated cargo storage using passive phase change elements, ensuring consistent sub-zero vaccine or frozen protein shipping without continuous grid-power drawing.",
    tier: "Silver"
  },
  {
    id: "ex_mycelium",
    name: "Mycelium Foods",
    focus: "Whole-tissue meat replacements from fungi spores.",
    track: "Tech & Innovation",
    description: "Mycelium Foods brews dense, textured whole cuts like prime steaks and fillets entirely from fungi filaments root-systems, avoiding synthetic processes.",
    tier: "Platinum"
  },
  {
    id: "ex_aqualoop",
    name: "AquaLoop Marine",
    focus: "On-shore solar-powered marine aquaculture systems.",
    track: "Sustainability & Packaging",
    description: "Providing high-efficiency land-based seawater recycling containment systems for prawns and organic kelp, with full thermal and nutrient salvage.",
    tier: "Silver"
  },
  {
    id: "ex_chocotrace",
    name: "ChocoTrace Block",
    focus: "Decentralized forest compliance tracking software.",
    track: "Consumer & Regulatory",
    description: "Blockchain consensus verification ledger that aggregates geolocation scans from cocoa farmers, proving absolute compliance with anti-deforestation standards.",
    tier: "Gold"
  }
];

// Helper to get all exhibitors merged dynamically from DB & Static lists
function getAllExhibitorsForMatchmaking() {
  const dbExhibitors = getExhibitors();
  const dbMapped = dbExhibitors.map(e => {
    let track = "Tech & Innovation";
    if (e.primarySectors.toLowerCase().includes("sustainability") || e.primarySectors.toLowerCase().includes("packaging")) {
      track = "Sustainability & Packaging";
    } else if (e.primarySectors.toLowerCase().includes("supply chain") || e.primarySectors.toLowerCase().includes("automation") || e.primarySectors.toLowerCase().includes("equipment")) {
      track = "Supply Chain & Automation";
    }
    return {
      id: e.id,
      name: e.displayName,
      focus: e.companyDescription,
      track,
      description: e.companyDescription,
      tier: e.role === "Sponsor" ? "Platinum" : "Gold"
    };
  });
  return [...staticExhibitors, ...dbMapped];
}

// Global active database check on server startup
loadDatabase();

// Offline Local Fallback Matchmaking Matcher
function runLocalMatchmakingFallback(
  name: string,
  company: string,
  goal: string,
  track: string,
  tier: string
) {
  const welcoming = `Hi ${name} from ${company}! Welcome to the Food Forward Summit. Based on your objective to ${goal}, we have mapped out a tailored experience across the event.`;
  const exhibitors = getAllExhibitorsForMatchmaking();

  const sortedExhibitors = exhibitors.map((ex) => {
    let score = 50;

    // Direct track align
    if (ex.track.toLowerCase() === track.toLowerCase()) {
      score += 30;
    }

    // Match keywords from the goal
    const goalWords = goal.toLowerCase().split(/[\s,.]+/);
    let matchWordsCount = 0;
    goalWords.forEach((word) => {
      if (word.length > 3) {
        if (
          ex.description.toLowerCase().includes(word) ||
          ex.focus.toLowerCase().includes(word) ||
          ex.name.toLowerCase().includes(word)
        ) {
          matchWordsCount++;
        }
      }
    });

    score += Math.min(20, matchWordsCount * 5);

    // Minor lift for higher sponsor tier
    if (ex.tier === "Platinum") score += 5;
    else if (ex.tier === "Gold") score += 2;

    let matchReason = `Their innovative solutions in ${ex.track} provide key synergy with your goals.`;
    if (ex.id === "ex_biocult") {
      matchReason = "They offer cellular reactor blueprints perfectly aligned with scaling raw cellular agriculture.";
    } else if (ex.id === "ex_ecopack") {
      matchReason = "Their seaweed composites perfectly answer sustainable, bio-degradable wrapping initiatives.";
    } else if (ex.id === "ex_agridrone") {
      matchReason = "Their autonomous flight vector logistics will optimize precise compound deployment in your supply chain.";
    } else if (ex.id === "ex_verdevertical") {
      matchReason = "Their high-yield metropolitan circular kits bypass logistics bottlenecks for urban cultivation.";
    } else if (ex.id === "ex_freezefresh") {
      matchReason = "Their liquid-nitrogen cold shipping technology ensures safe organic protein freight storage.";
    } else if (ex.id === "ex_mycelium") {
      matchReason = "Their solid whole-cut mushroom meat brewing provides excellent sustainable retail inventory.";
    } else if (ex.id === "ex_aqualoop") {
      matchReason = "Their recirculating marine technology minimizes eco-footprint for coastal harvest networks.";
    } else if (ex.id === "ex_chocotrace") {
      matchReason = "Their decentralized web platform provides bulletproof geofence and compliant compliance trails.";
    }

    return {
      exhibitorId: ex.id,
      exhibitorName: ex.name,
      matchScore: Math.min(100, score),
      matchReason,
    };
  });

  // Sort and pick top 3
  sortedExhibitors.sort((a, b) => b.matchScore - a.matchScore);
  const top3 = sortedExhibitors.slice(0, 3);

  return {
    welcomeMessage: welcoming,
    primaryTrackRecommended: track,
    recommendedExhibitors: top3,
  };
}

// Database API routes
app.get("/api/exhibitors", (req, res) => {
  res.json(getExhibitors());
});

app.get("/api/speakers", (req, res) => {
  res.json(getSpeakers());
});

app.post("/api/exhibitors", (req, res) => {
  try {
    const saved = addOrUpdateExhibitor(req.body);
    res.json({ success: true, data: saved });
  } catch (error: any) {
    res.status(500).json({ error: error.message });
  }
});

app.post("/api/speakers", (req, res) => {
  try {
    const saved = addOrUpdateSpeaker(req.body);
    res.json({ success: true, data: saved });
  } catch (error: any) {
    res.status(500).json({ error: error.message });
  }
});

// UUID Security Authentication System Endpoint
app.post("/api/auth", (req, res) => {
  const { uuid, loginType, email } = req.body;
  if (!uuid) {
    return res.status(400).json({ error: "Universal ID code is required to establish credential lock." });
  }

  const db = loadDatabase();
  const searchUuid = uuid.trim().toLowerCase();

  // 1. Search for matching Speaker ID
  const speaker = db.speakers.find(s => s.id.toLowerCase() === searchUuid);
  if (speaker) {
    return res.json({
      authenticated: true,
      role: "Speaker",
      userId: speaker.id,
      email: speaker.email,
      displayName: speaker.fullName,
      profile: {
        id: speaker.id,
        fullName: speaker.fullName,
        email: speaker.email,
        topicTitle: speaker.topicTitle,
        bio: speaker.bio,
        sessionFormat: speaker.sessionFormat,
        avRequirements: speaker.avRequirements,
        linkedinUrl: speaker.linkedinUrl,
        location: speaker.location
      }
    });
  }

  // 2. Search for matching Exhibitor ID
  const exhibitor = db.exhibitors.find(e => e.id.toLowerCase() === searchUuid);
  if (exhibitor) {
    return res.json({
      authenticated: true,
      role: exhibitor.role || "Exhibitor",
      userId: exhibitor.id,
      email: exhibitor.email || email || "exhibitor@nleats.com",
      displayName: exhibitor.displayName,
      profile: {
        id: exhibitor.id,
        displayName: exhibitor.displayName,
        companyDescription: exhibitor.companyDescription,
        email: exhibitor.email,
        websiteUrl: exhibitor.websiteUrl,
        linkedinUrl: exhibitor.linkedinUrl,
        countryRegion: exhibitor.countryRegion,
        annualRevenue: exhibitor.annualRevenue,
        currentMarkets: exhibitor.currentMarkets,
        targetMarkets: exhibitor.targetMarkets,
        importExportStatus: exhibitor.importExportStatus,
        brandsRepresented: exhibitor.brandsRepresented,
        primarySectors: exhibitor.primarySectors,
        targetBuyers: exhibitor.targetBuyers,
        boothSizeConfirmed: exhibitor.boothSizeConfirmed,
        electricalNeeds: exhibitor.electricalNeeds,
        exhibitorLeadId: exhibitor.exhibitorLeadId
      }
    });
  }

  // 3. Simple wildcard master password for instant VIP testing access
  if (searchUuid === "demo" || searchUuid === "884384d3") {
    // Falls back to Alexander Kappes as demonstration
    const demoSpeaker = db.speakers[0];
    return res.json({
      authenticated: true,
      role: "Speaker",
      userId: demoSpeaker.id,
      email: demoSpeaker.email,
      displayName: demoSpeaker.fullName,
      profile: demoSpeaker,
      isDemoMode: true
    });
  }

  return res.status(401).json({
    error: "Authentication failed. The provided UUID is not registered in our Exhibitor or Speaker database. Please contact support or consult the physical pass database."
  });
});

// B2B AI matchmaking proxy route
app.post("/api/matchmaking", async (req, res) => {
  const { name, company, goal, track, tier } = req.body;

  if (!name || !company || !goal || !track || !tier) {
    return res.status(400).json({ error: "Missing required onboarding profile fields" });
  }

  const apiKey = process.env.GEMINI_API_KEY;

  if (!apiKey || apiKey === "MY_GEMINI_API_KEY" || apiKey.trim() === "") {
    console.log("No valid process.env.GEMINI_API_KEY. Running robust offline local matchmaker fallback...");
    const localResult = runLocalMatchmakingFallback(name, company, goal, track, tier);
    return res.json(localResult);
  }

  try {
    const ai = new GoogleGenAI({
      apiKey,
      httpOptions: {
        headers: {
          "User-Agent": "aistudio-build",
        },
      },
    });

    const exhibitors = getAllExhibitorsForMatchmaking();
    const exhibitorsInfo = exhibitors
      .map((ex) => `ID: ${ex.id}, Name: ${ex.name}, Focus: ${ex.focus}, Track: ${ex.track}, Details: ${ex.description}`)
      .join("\n");

    const prompt = `
You are an advanced enterprise B2B matchmaking agent running inside a mobile event app's backend for the Food Forward Summit 2026.
Your objective is to ingest an individual attendee's profile/onboarding answers and cross-reference them against an array of available event exhibitors. You must select the top 3 most synergetic matches and generate a highly personalized welcome greeting.

Here are the attendee onboarding profile answers:
- Name: ${name}
- Company: ${company}
- Objective: ${goal}
- Preferred Track: ${track}
- Budget/Tier Goal: ${tier}

Here is the database of exhibitors at the Food Forward Summit 2026:
${exhibitorsInfo}

CRITICAL RULES:
1. You must only return a valid, strictly structured JSON object. 
2. Do not include markdown code blocks (like \`\`\`json ... \`\`\`) in the raw API response.
3. Your JSON structure must match this exact schema:
{
  "welcomeMessage": "A concise, engaging 2-sentence greeting mentioning their specific goal.",
  "primaryTrackRecommended": "The exact name of the track matching their profile",
  "recommendedExhibitors": [
    {
      "exhibitorId": "String matching the database primary key ID provided",
      "exhibitorName": "String",
      "matchScore": 1-100 integer based on dataset relevance,
      "matchReason": "A 1-sentence explanation of why this specific company benefits their business objectives."
    }
  ]
}
`.trim();

    const response = await ai.models.generateContent({
      model: "gemini-3.5-flash",
      contents: prompt,
      config: {
        responseMimeType: "application/json",
        temperature: 0.4,
      },
    });

    let text = response.text || "";
    text = text.trim();

    // Clean JSON blocks if returned
    if (text.startsWith("```json")) {
      text = text.substring(7);
    } else if (text.startsWith("```")) {
      text = text.substring(3);
    }
    if (text.endsWith("```")) {
      text = text.substring(0, text.length - 3);
    }
    text = text.trim();

    const result = JSON.parse(text);
    return res.json(result);
  } catch (error) {
    console.error("Gemini Matchmaker Exception. Falling back to local algorithm:", error);
    const localResult = runLocalMatchmakingFallback(name, company, goal, track, tier);
    return res.json(localResult);
  }
});

// Start routing static or development mode
async function startServer() {
  if (process.env.NODE_ENV !== "production") {
    const vite = await createViteServer({
      server: { middlewareMode: true },
      appType: "spa",
    });
    app.use(vite.middlewares);
  } else {
    const distPath = path.join(process.cwd(), "dist");
    app.use(express.static(distPath));
    app.get("*", (req, res) => {
      res.sendFile(path.join(distPath, "index.html"));
    });
  }

  app.listen(PORT, "0.0.0.0", () => {
    console.log(`Server launched and ready at http://localhost:${PORT}`);
  });
}

startServer();

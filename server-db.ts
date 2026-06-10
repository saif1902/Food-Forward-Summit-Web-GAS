import fs from "fs";
import path from "path";

// Define the absolute system paths for our persistent database
const DB_FILE_PATH = path.join(process.cwd(), "exhibitors_speakers_db.json");

export interface SeedExhibitor {
  id: string;
  displayName: string;
  companyDescription: string;
  email: string;
  websiteUrl: string;
  linkedinUrl: string;
  instagramUrl: string;
  xUrl: string;
  countryRegion: string;
  annualRevenue: string;
  currentMarkets: string; // JSON parsed/string
  targetMarkets: string;  // JSON parsed/string
  importExportStatus: string;
  brandsRepresented: string;
  primarySectors: string; // JSON parsed/string
  targetBuyers: string;
  boothSizeConfirmed: string;
  electricalNeeds: string;
  directoryConsent: boolean;
  exhibitorLeadId: string;
  role: "Sponsor" | "Exhibitor" | "Attendee";
}

export interface SeedSpeaker {
  id: string;
  createdAt: string;
  fullName: string;
  email: string;
  topicTitle: string;
  bio: string;
  sessionFormat: string;
  avRequirements: string;
  submissionType: string;
  nomineeName: string;
  nomineeEmail: string;
  linkedinUrl: string;
  location: string;
}

// Low-level Seed Lists matching the user's provided CSV rows perfectly
const initialExhibitors: SeedExhibitor[] = [
  {
    id: "feb8a00c-839e-4412-80c8-2e76765a1014",
    displayName: "CogniCo",
    companyDescription: "AI-Consulting for modern food systems to maximize agricultural yield, distribute automated software, and advise on sustainable crop deployment.",
    email: "cognico@nleats.com", // Linked to user's registered email
    websiteUrl: "www.cognico.ca",
    linkedinUrl: "",
    instagramUrl: "",
    xUrl: "",
    countryRegion: "Canada",
    annualRevenue: "Pre-revenue",
    currentMarkets: '["Canada"]',
    targetMarkets: '["Canada","United States"]',
    importExportStatus: "Both",
    brandsRepresented: "AI",
    primarySectors: '["Primary Production, Agritech & Smart Farming"]',
    targetBuyers: "Investors & VCs",
    boothSizeConfirmed: "Test",
    electricalNeeds: "Internet",
    directoryConsent: true,
    exhibitorLeadId: "f81b29e2-57c4-4d4b-aefa-69591b91aae4",
    role: "Sponsor"
  },
  {
    id: "e1160788-009e-4db2-a61c-a134bfd84634",
    displayName: "Test",
    companyDescription: "Sourcing partner researching sustainable tech implementation, primary farming analytics, and test simulations for metropolitan distribution channels.",
    email: "test@nleats.com",
    websiteUrl: "",
    linkedinUrl: "",
    instagramUrl: "",
    xUrl: "",
    countryRegion: "Canada",
    annualRevenue: "Pre-revenue",
    currentMarkets: '["Canada"]',
    targetMarkets: '["Mexico"]',
    importExportStatus: "Exporter",
    brandsRepresented: "Test",
    primarySectors: '["Primary Production, Agritech & Smart Farming"]',
    targetBuyers: "Government & Institutions",
    boothSizeConfirmed: "Test",
    electricalNeeds: "Test",
    directoryConsent: true,
    exhibitorLeadId: "0d8090e5-dd5b-4641-b968-35b72f78b531",
    role: "Exhibitor"
  },
  {
    id: "f4f09cb2-c7c0-49c0-b776-225ee4155cdb",
    displayName: "Food Forward",
    companyDescription: "Next-generation snack sourcing specializing in marine algae compositions, seaweed wraps, eco-packaging materials and alternative proteins.",
    email: "forward@nleats.com",
    websiteUrl: "https://www.cognico.ca/",
    linkedinUrl: "https://www.linkedin.com/in/cognico-test-3859263b9/",
    instagramUrl: "https://www.instagram.com/cognico_ai/",
    xUrl: "https://x.com/CogniCO_AI",
    countryRegion: "Canada",
    annualRevenue: "$100K – $500K",
    currentMarkets: '["Canada","European Union","South America"]',
    targetMarkets: '["Canada","European Union","South America"]',
    importExportStatus: "Both",
    brandsRepresented: "NLEats",
    primarySectors: '["Primary Production, Agritech & Smart Farming","Sustainable Packaging","Start-Ups"]',
    targetBuyers: "Investors & VCs",
    boothSizeConfirmed: "8x8 Kiosk",
    electricalNeeds: "ALL",
    directoryConsent: true,
    exhibitorLeadId: "74c53fc8-c454-4221-bab7-90b2f8e5b250",
    role: "Exhibitor"
  },
  {
    id: "9da28dbe-3b1e-4e60-834e-69e13f2ecf9c",
    displayName: "Cognico_test",
    companyDescription: "Early stage startup testing smart temperature cold-chains, automated crop sensor devices, and modern logistics management software.",
    email: "admin@cognico.ca",
    websiteUrl: "www.cognico.ca",
    linkedinUrl: "https://ca.linkedin.com/company/cognico-ai-consulting",
    instagramUrl: "https://www.instagram.com/cognico.ca/",
    xUrl: "",
    countryRegion: "Canada",
    annualRevenue: "Pre-revenue",
    currentMarkets: '["Canada","United States"]',
    targetMarkets: '["United Kingdom","Canada","South America","Asia Pacific"]',
    importExportStatus: "Both",
    brandsRepresented: "Cognico_test",
    primarySectors: '["Primary Production, Agritech & Smart Farming","Investment, Trade & Export","Food Processing & Equipment"]',
    targetBuyers: "Investors & VCs",
    boothSizeConfirmed: "Test",
    electricalNeeds: "Not needed",
    directoryConsent: true,
    exhibitorLeadId: "291f8a8f-cd3a-4414-a282-0c39995ce1db",
    role: "Exhibitor"
  }
];

const initialSpeakers: SeedSpeaker[] = [
  {
    id: "884384d3-2093-41e2-8b7f-8299e0ba4615",
    createdAt: "2026-04-28 09:20:36.498008+00",
    fullName: "Alexande Kappes",
    email: "a.m.kappes@gmail.com",
    topicTitle: "AI at the Fence Line: What Livestock Technology Is Teaching the Rest of the Food System About Deploying AI to Farmers & Ranchers",
    bio: "As founder of Greener Herd, I built a digital livestock management platform serving smallholder farmers across the Middle East and Africa — regions where animal agriculture sustains hundreds of millions of livelihoods but has been almost entirely overlooked by mainstream agtech. In partnership with the UN World Food Programme and FAO, Greener Herd brings basic digital infrastructure, animal records, fertility and mortality tracking, nutrition guidance, to operations that have never had it. My contribution to food systems innovation is closing the data gap at the base of the global animal protein pyramid, where the productivity gains are largest and the tools have been thinnest.",
    sessionFormat: "Panel Discussion",
    avRequirements: "",
    submissionType: "propose_panel",
    nomineeName: "",
    nomineeEmail: "",
    linkedinUrl: "https://www.linkedin.com/in/alexander-m-kappes/",
    location: "Toronto, ON"
  },
  {
    id: "ad67408c-15ad-4164-b4f0-45a68dbba69d",
    createdAt: "2026-04-28 14:48:32.358737+00",
    fullName: "ASHLEY NICHOLLS",
    email: "ashley@reachag.ca",
    topicTitle: "Boots On The Ground Innovation",
    bio: "Through my work with REACH Agriculture Strategies, I focus on a critical but often overlooked driver of food systems innovation: people. Specifically, the frontline workforce that operates within our farms, feedlots, and agricultural businesses every day. While much of the innovation conversation centers on technology, sustainability metrics, and policy, my work emphasizes that lasting innovation only occurs when it is understood, and adopted by the people closest to the work.",
    sessionFormat: "Keynote",
    avRequirements: "",
    submissionType: "pitch_talk",
    nomineeName: "",
    nomineeEmail: "",
    linkedinUrl: "https://www.linkedin.com/in/ashley-nicholls-/",
    location: "Toronto, ON"
  },
  {
    id: "b8177664-818a-451b-a9b0-50522b1daa2b",
    createdAt: "2026-04-29 00:01:20.47657+00",
    fullName: "Saif",
    email: "saif.ahmed@nleats.com",
    topicTitle: "Agritech Challenges",
    bio: "Pioneering technology advisor sharing agricultural logistics templates and modern regional deployment insights.",
    sessionFormat: "Keynote",
    avRequirements: "Test",
    submissionType: "pitch_talk",
    nomineeName: "",
    nomineeEmail: "",
    linkedinUrl: "https://ca.linkedin.com/in/saifahmed-",
    location: "Toronto, ON"
  },
  {
    id: "ba34ffa2-9653-467f-be17-d52d57024f9c",
    createdAt: "2026-05-02 19:12:44.750015+00",
    fullName: "Heidi M. Peterson",
    email: "hpeterson@sandcountyfoundation.org",
    topicTitle: "Soil health/Regenerative Agriculture",
    bio: "Sand County Foundation (SCF) is a 501(c)(3) non-profit conservation organization whose work with farmers and ranchers 60 years has grown through inspiration, innovation, and investment. SCF helps private landowners achieve their full potential as conservationists by providing public recognition for outstanding private land stewardship; facilitating exchange of info among landowners...",
    sessionFormat: "Keynote",
    avRequirements: "*Happy to serve as a panelist, keynote or fireside chat.",
    submissionType: "pitch_talk",
    nomineeName: "",
    nomineeEmail: "",
    linkedinUrl: "https://www.linkedin.com/in/heidi-peterson-5a931648/",
    location: "Toronto, ON"
  },
  {
    id: "9042fdd6-2d4b-48ea-b991-40730d3731aa",
    createdAt: "2026-05-04 15:23:11.928926+00",
    fullName: "Sophia Weiss",
    email: "sophia.weiss@grainmillers.com",
    topicTitle: "Scaling regenerative agriculture across the Canadian Prairies and building vibrant cross sector partnerships",
    bio: "Sophia Weiss is the Director of Sustainability and Regenerative Agriculture at Grain Millers Inc., one of the largest oat processors in North America. She holds a master’s degree in Agriculture, Food, and Environment from the Friedman School of Nutrition Science and Policy at Tufts University and is a registered dietitian.",
    sessionFormat: "Fireside Chat",
    avRequirements: "",
    submissionType: "propose_panel",
    nomineeName: "",
    nomineeEmail: "",
    linkedinUrl: "https://www.linkedin.com/in/sophia-weiss/",
    location: "Toronto, ON"
  },
  {
    id: "a7fa3320-31be-4c0f-a682-6d2d6b513551",
    createdAt: "2026-05-05 14:33:59.087472+00",
    fullName: "Marc-André Roberge",
    email: "marc@nectar.buzz",
    topicTitle: "Leveraging agritech to benefit nature",
    bio: "With a background in product design, Marc-André developed a passion for beekeeping while working on designs to improve human-bees interactions. He's CEO and Co-founder at Nectar, a company on a mission to help beekeepers raise thriving honey bee colonies to secure our food supply.",
    sessionFormat: "Keynote",
    avRequirements: "Screen to display deck",
    submissionType: "pitch_talk",
    nomineeName: "",
    nomineeEmail: "",
    linkedinUrl: "https://www.linkedin.com/in/marcandreroberge/",
    location: "Toronto, ON"
  },
  {
    id: "97303fd5-bab6-47b3-b668-86de10442635",
    createdAt: "2026-05-05 14:40:39.446351+00",
    fullName: "Maha Tahiri",
    email: "maha@s2bgroup.com",
    topicTitle: "Food Forward in the GLP-1 Era: Reinventing Food Systems for a New Consumer-Patient",
    bio: "Maha Tahiri is a globally recognized nutrition strategist with three decades of experience translating food science into business strategy and food systems transformation. As Co-Founder and CEO of S2B Group, she advises Fortune 300 CPG companies, ingredient innovators, and commodity boards on aligning nutrition science, consumer behavior, and sustainability.",
    sessionFormat: "Keynote",
    avRequirements: "",
    submissionType: "pitch_talk",
    nomineeName: "",
    nomineeEmail: "",
    linkedinUrl: "https://www.linkedin.com/in/mahatahiri/",
    location: "Toronto, ON"
  },
  {
    id: "206de90c-c5ac-4d84-855a-af7e149bb001",
    createdAt: "2026-05-08 18:43:36.35489+00",
    fullName: "Daniela Galloro",
    email: "marketing@biomemakers.com",
    topicTitle: "Agritech, Regenerative Farming, AI in Food Systems, Supply Chain Resilience, Sustainability Metrics. Beyond the Checklist: Leveraging AI to Verify Soil Health and Supply Chain Resilience",
    bio: "Dr. Jacob Parnell is a globally recognized authority in soil microbiology and sustainable agriculture, currently serving as the Director of Agronomy at Biome Makers. With a Ph.D. from Michigan State University’s Center for Microbial Ecology, Dr. Parnell has spent over two decades bridging the gap between microbial science and commercial agricultural viability.",
    sessionFormat: "Keynote",
    avRequirements: "microphone, screensharing",
    submissionType: "nominate_speaker",
    nomineeName: "Dr. Jacob Parnell",
    nomineeEmail: "jacob.parnell@biomemakers.com",
    linkedinUrl: "https://www.linkedin.com/in/jjacob-parnell/",
    location: "Toronto, ON"
  },
  {
    id: "be5c927d-2c3c-48c0-b724-e966dc79ee08",
    createdAt: "2026-05-14 13:17:56.630925+00",
    fullName: "Mehnaz Tabassum",
    email: "mehnaz.tabassum@nleats.com",
    topicTitle: "Sustainable Human Management Systems & Frontline Agricultural Innovation",
    bio: "Leading food security and nutrition software expert with deeper focuses on metropolitan distributions and small-scale grower integration pipelines.",
    sessionFormat: "Keynote",
    avRequirements: "mic",
    submissionType: "pitch_talk",
    nomineeName: "",
    nomineeEmail: "",
    linkedinUrl: "dsd",
    location: "Toronto, ON"
  },
  {
    id: "94b47f3f-7333-431f-880a-4b377af8c824",
    createdAt: "2026-05-20 00:02:52.416437+00",
    fullName: "Brendon Steele",
    email: "bsteele@future500.org",
    topicTitle: "Food System & Supply Chain Sustainability",
    bio: "Facilitators of corporate nature strategies (beyond just food supply chains) exploring efficient AI integration tools to verify soil compliance and consumer trust metrics.",
    sessionFormat: "Panel Discussion",
    avRequirements: "",
    submissionType: "propose_panel",
    nomineeName: "",
    nomineeEmail: "",
    linkedinUrl: "https://www.linkedin.com/in/brendonsteele",
    location: "Toronto, ON"
  },
  {
    id: "5feef27b-db94-426c-ad9b-4b43f92c5623",
    createdAt: "2026-05-21 16:46:46.092467+00",
    fullName: "Carin Gerhardt",
    email: "carin.gerhardt@svgventures.com",
    topicTitle: "Scaling Access to Sustainable & Nutritious Food Through Partnerships",
    bio: "Carin Gerhardt is Director of Corporate Programs at THRIVE by SVG Ventures, where she leads global innovation partnerships connecting startups, corporations, investors, and research organizations.",
    sessionFormat: "Panel Discussion",
    avRequirements: "",
    submissionType: "propose_panel",
    nomineeName: "",
    nomineeEmail: "",
    linkedinUrl: "https://www.linkedin.com/in/caringerhardt/",
    location: "Toronto, ON"
  },
  {
    id: "33f4effc-14e6-4774-b2ea-0fc72d07c670",
    createdAt: "2026-05-29 16:37:02.982494+00",
    fullName: "Julie Francoeur",
    email: "communications@fairtrade.ca",
    topicTitle: "From Risk to Relationship: Why Business Models Built on Equity are Outperforming in Uncertain Markets",
    bio: "Julie Francoeur is the CEO of Fairtrade Canada and a member of the Fairtrade International Executive Team. Sourcing as a true partner rather than a transactional seller produces robust corporate operations.",
    sessionFormat: "Keynote",
    avRequirements: "TBD",
    submissionType: "nominate_speaker",
    nomineeName: "",
    nomineeEmail: "",
    linkedinUrl: "https://www.linkedin.com/in/francoeurjulie/",
    location: "Toronto, ON"
  },
  {
    id: "89302393-6920-41df-a3b6-61ad346830c2",
    createdAt: "2026-06-08 13:02:09.087013+00",
    fullName: "Meifan Shi",
    email: "meifan@waterpointlane.com",
    topicTitle: "Invisible Infrastructure: AI Reshaping food and agricultural investments",
    bio: "Partner at Waterpoint Lane, backing high-conviction companies utilizing rapid sensor automation and machine intelligence that reshape food networks globally.",
    sessionFormat: "Keynote",
    avRequirements: "",
    submissionType: "pitch_talk",
    nomineeName: "Meifan Shi",
    nomineeEmail: "meifan@waterpointlane.com",
    linkedinUrl: "https://www.linkedin.com/in/meifanshi/",
    location: "Toronto, ON"
  }
];

interface DatabaseSchema {
  exhibitors: SeedExhibitor[];
  speakers: SeedSpeaker[];
}

// Read or initialize the secure local file database
export function loadDatabase(): DatabaseSchema {
  try {
    if (fs.existsSync(DB_FILE_PATH)) {
      const raw = fs.readFileSync(DB_FILE_PATH, "utf8");
      return JSON.parse(raw);
    }
  } catch (error) {
    console.error("Failed to read database file, rebuilding from scratch:", error);
  }

  // If doesn't exist, seed and save
  const seedDB: DatabaseSchema = {
    exhibitors: initialExhibitors,
    speakers: initialSpeakers
  };
  saveDatabase(seedDB);
  return seedDB;
}

export function saveDatabase(data: DatabaseSchema) {
  try {
    fs.writeFileSync(DB_FILE_PATH, JSON.stringify(data, null, 2), "utf8");
  } catch (error) {
    console.error("Failed to write to database file:", error);
  }
}

// Database helper functions
export function getExhibitors() {
  return loadDatabase().exhibitors;
}

export function getSpeakers() {
  return loadDatabase().speakers;
}

export function addOrUpdateExhibitor(ex: SeedExhibitor) {
  const db = loadDatabase();
  const existingIndex = db.exhibitors.findIndex(item => item.id.toLowerCase() === ex.id.toLowerCase());
  if (existingIndex > -1) {
    db.exhibitors[existingIndex] = { ...db.exhibitors[existingIndex], ...ex };
  } else {
    db.exhibitors.push(ex);
  }
  saveDatabase(db);
  return ex;
}

export function addOrUpdateSpeaker(spk: SeedSpeaker) {
  const db = loadDatabase();
  const existingIndex = db.speakers.findIndex(item => item.id.toLowerCase() === spk.id.toLowerCase());
  if (existingIndex > -1) {
    db.speakers[existingIndex] = { ...db.speakers[existingIndex], ...spk };
  } else {
    db.speakers.push(spk);
  }
  saveDatabase(db);
  return spk;
}

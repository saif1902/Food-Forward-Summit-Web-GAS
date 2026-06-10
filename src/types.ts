export interface RecommendedExhibitor {
  exhibitorId: string;
  exhibitorName: string;
  matchScore: number;
  matchReason: string;
}

export interface B2BMatchResult {
  welcomeMessage: string;
  primaryTrackRecommended: string;
  recommendedExhibitors: RecommendedExhibitor[];
}

export interface ProfileEntity {
  id: number;
  name: string;
  company: string;
  businessGoal: string;
  preferredTrack: string;
  budgetTier: string;
  matchedJson?: string; // Serialized B2BMatchResult
  email: string;
  provider: string; // "Email" | "Google" | "LinkedIn" | "Apple"
  isOtpVerified: boolean;
  role: string; // "Attendee" | "Exhibitor" | "Speaker" | "Sponsor" | "Investor" | "Organizer" | "Admin"
  isOnboarded: boolean;
  countryRegion?: string;
  websiteUrl?: string;
  linkedinUrl?: string;
  instagramUrl?: string;
  xUrl?: string;
  annualRevenue?: string;
  currentMarkets?: string;
  targetMarkets?: string;
  importExportStatus?: string;
  brandsRepresented?: string;
  primarySectors?: string;
  targetBuyers?: string;
  boothSizeConfirmed?: string;
  electricalNeeds?: string;
  exhibitorLeadId?: string;
  uniqueId?: string;
}

export interface AgendaSession {
  id: string;
  title: string;
  speaker: string;
  speakerRole: string;
  startTime: string;
  endTime: string;
  track: string;
  location: string;
  description: string;
  isBookmarked: boolean;
  attachmentUrl?: string | null;
}

export interface BookedMeeting {
  id: number;
  exhibitorId: string;
  exhibitorName: string;
  meetingTime: string;
  location: string;
  purpose: string;
  isVirtual: boolean;
}

export interface ScannedContact {
  id: string; // email or generated uuid
  name: string;
  company: string;
  email: string;
  phone: string;
  notes: string;
  rank: string; // "Hot" | "Warm" | "Cold"
  scannedAt: number;
}

export interface SocialPost {
  id: number;
  authorName: string;
  authorCompany: string;
  textContent: string;
  imageResName?: string | null;
  likesCount: number;
  timestamp: number;
  isLikedByMe: boolean;
  authorRole: string;
}

export interface GamificationState {
  id: number;
  score: number;
  triviaPassed: boolean;
  pollingCompleted: boolean;
  scavengerExhibitorsFound: string; // comma-separated IDs
  raffleTickets: number;
}

export interface SpeakerEntity {
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
  imageUrl?: string | null;
}

export interface ExhibitorEntity {
  id: string;
  name: string;
  focus: string;
  track: string;
  description: string;
  boothLocation: string;
  website: string;
  contactEmail: string;
  tier: string;
  logoAsset: string;
}

export interface AttendeeEntity {
  id: string;
  displayName: string;
  companyDescription: string;
  email: string;
  websiteUrl: string;
  linkedinUrl: string;
  countryRegion: string;
  annualRevenue: string;
  currentMarkets: string;
  targetMarkets: string;
  importExportStatus: string;
  brandsRepresented: string;
  primarySectors: string;
  targetBuyers: string;
  boothSizeConfirmed: string;
  electricalNeeds: string;
  exhibitorLeadId: string;
  role: string;
}

export interface MatchmakerQuestion {
  id: number;
  questionText: string;
  track: string;
}

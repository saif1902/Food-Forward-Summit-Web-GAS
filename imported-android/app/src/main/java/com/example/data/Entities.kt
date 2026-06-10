package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RecommendedExhibitor(
    val exhibitorId: String,
    val exhibitorName: String,
    val matchScore: Int,
    val matchReason: String
)

@JsonClass(generateAdapter = true)
data class B2BMatchResult(
    val welcomeMessage: String,
    val primaryTrackRecommended: String,
    val recommendedExhibitors: List<RecommendedExhibitor>
)

@Entity(tableName = "attendee_profile")
data class ProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "",
    val company: String = "",
    val businessGoal: String = "",
    val preferredTrack: String = "Sustainability & Packaging",
    val budgetTier: String = "Growth",
    val matchedJson: String? = null, // Serialized B2BMatchResult
    val email: String = "",
    val provider: String = "", // "Email", "Google", "LinkedIn", "Apple"
    val isOtpVerified: Boolean = false,
    val role: String = "Attendee", // "Attendee", "Exhibitor", "Speaker", "Sponsor", "Investor"
    val isOnboarded: Boolean = false,
    val countryRegion: String = "",
    val websiteUrl: String = "",
    val linkedinUrl: String = "",
    val instagramUrl: String = "",
    val xUrl: String = "",
    val annualRevenue: String = "",
    val currentMarkets: String = "",
    val targetMarkets: String = "",
    val importExportStatus: String = "",
    val brandsRepresented: String = "",
    val primarySectors: String = "",
    val targetBuyers: String = "",
    val boothSizeConfirmed: String = "",
    val electricalNeeds: String = "",
    val exhibitorLeadId: String = "",
    val uniqueId: String = ""
)

@Entity(tableName = "agenda_sessions")
data class AgendaSession(
    @PrimaryKey val id: String,
    val title: String,
    val speaker: String,
    val speakerRole: String,
    val startTime: String,
    val endTime: String,
    val track: String,
    val location: String,
    val description: String,
    val isBookmarked: Boolean = false,
    val attachmentUrl: String? = null
)

@Entity(tableName = "booked_meetings")
data class BookedMeeting(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val exhibitorId: String,
    val exhibitorName: String,
    val meetingTime: String,
    val location: String,
    val purpose: String,
    val isVirtual: Boolean = false
)

@Entity(tableName = "scanned_contacts")
data class ScannedContact(
    @PrimaryKey val id: String, // email or generated uuid
    val name: String,
    val company: String,
    val email: String,
    val phone: String = "",
    val notes: String = "",
    val rank: String = "Warm", // Hot, Warm, Cold (For lead retrieval)
    val scannedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "social_posts")
data class SocialPost(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val authorName: String,
    val authorCompany: String,
    val textContent: String,
    val imageResName: String? = null, // e.g., mock image
    val likesCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val isLikedByMe: Boolean = false,
    val authorRole: String = "Attendee"
)

@Entity(tableName = "gamification_state")
data class GamificationState(
    @PrimaryKey val id: Int = 1,
    val score: Int = 150,
    val triviaPassed: Boolean = false,
    val pollingCompleted: Boolean = false,
    val scavengerExhibitorsFound: String = "", // Comma-separated list of IDs
    val raffleTickets: Int = 2
)

@Entity(tableName = "post_logs")
data class PostLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val authorName: String,
    val authorCompany: String,
    val textContent: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_trivia_answers")
data class TriviaAnswerEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String,
    val userName: String,
    val questionText: String,
    val selectedOption: String,
    val isCorrect: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "matchmaker_results")
data class MatchmakerResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String,
    val exhibitorId: String,
    val contentTitle: String,
    val contentDescription: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "speakers")
data class SpeakerEntity(
    @PrimaryKey val id: String,
    val createdAt: String,
    val fullName: String,
    val email: String,
    val topicTitle: String,
    val bio: String,
    val sessionFormat: String,
    val avRequirements: String,
    val submissionType: String,
    val nomineeName: String,
    val nomineeEmail: String,
    val linkedinUrl: String,
    val location: String,
    val imageUrl: String? = null // Profile image URL or initials style
)

@Entity(tableName = "exhibitors")
data class ExhibitorEntity(
    @PrimaryKey val id: String,
    val name: String,
    val focus: String,
    val track: String,
    val description: String,
    val boothLocation: String,
    val website: String,
    val contactEmail: String,
    val tier: String,
    val logoAsset: String
)

@Entity(tableName = "attendees")
data class AttendeeEntity(
    @PrimaryKey val id: String, // e.g., uuid
    val displayName: String,
    val companyDescription: String,
    val email: String = "",
    val websiteUrl: String = "",
    val linkedinUrl: String = "",
    val countryRegion: String = "",
    val annualRevenue: String = "",
    val currentMarkets: String = "",
    val targetMarkets: String = "",
    val importExportStatus: String = "",
    val brandsRepresented: String = "",
    val primarySectors: String = "",
    val targetBuyers: String = "",
    val boothSizeConfirmed: String = "",
    val electricalNeeds: String = "",
    val exhibitorLeadId: String = "",
    val role: String = "Attendee"
)

@Entity(tableName = "matchmaker_questions")
data class MatchmakerQuestion(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val questionText: String,
    val track: String = "Sustainability & Packaging"
)



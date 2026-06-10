package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class EventViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = AppRepository(db)

    // Data streams
    val profileState: StateFlow<ProfileEntity?> = repository.profileFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val sessionsState: StateFlow<List<AgendaSession>> = repository.sessionsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val meetingsState: StateFlow<List<BookedMeeting>> = repository.meetingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val contactsState: StateFlow<List<ScannedContact>> = repository.contactsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val postsState: StateFlow<List<SocialPost>> = repository.postsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val gameState: StateFlow<GamificationState?> = repository.gameStateFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val postLogsState: StateFlow<List<PostLogEntity>> = repository.postLogsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val triviaAnswersState: StateFlow<List<TriviaAnswerEntity>> = repository.triviaAnswersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val matchmakerResultsState: StateFlow<List<MatchmakerResultEntity>> = repository.matchmakerResultsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val speakersState: StateFlow<List<SpeakerEntity>> = repository.speakersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val exhibitorsState: StateFlow<List<ExhibitorEntity>> = repository.exhibitorsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val attendeesState: StateFlow<List<AttendeeEntity>> = repository.attendeesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val matchmakerQuestionsState: StateFlow<List<MatchmakerQuestion>> = repository.matchmakerQuestionsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Matchmaking UI State
    private val _matchUiState = MutableStateFlow<MatchUiState>(MatchUiState.Idle)
    val matchUiState: StateFlow<MatchUiState> = _matchUiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Seed DB if empty
            repository.prepopulateIfEmpty()
        }
    }

    // Matchmaker operations
    fun runMatchmaking(
        name: String,
        company: String,
        goal: String,
        track: String,
        tier: String
    ) {
        viewModelScope.launch {
            _matchUiState.value = MatchUiState.Loading
            try {
                val result = repository.generateB2BMatch(name, company, goal, track, tier)
                _matchUiState.value = MatchUiState.Success(result)
            } catch (e: Exception) {
                _matchUiState.value = MatchUiState.Error(e.message ?: "Unknown matching error")
            }
        }
    }

    fun resetMatchState() {
        _matchUiState.value = MatchUiState.Idle
    }

    // Agenda actions
    fun toggleSessionBookmark(session: AgendaSession) {
        viewModelScope.launch {
            repository.toggleBookmark(session.id, !session.isBookmarked)
        }
    }

    // Meeting actions
    fun bookMeeting(exhibitor: Exhibitor, purpose: String, selectedTime: String) {
        viewModelScope.launch {
            repository.bookMeeting(
                BookedMeeting(
                    exhibitorId = exhibitor.id,
                    exhibitorName = exhibitor.name,
                    meetingTime = selectedTime,
                    location = exhibitor.boothLocation,
                    purpose = purpose
                )
            )
        }
    }

    fun bookMeetingFromMatch(exhibitorId: String, exhibitorName: String, selectedTime: String, purpose: String) {
        viewModelScope.launch {
            val location = repository.exhibitorsFlow.first().find { it.id == exhibitorId }?.boothLocation ?: "Matchmaking Lounge"
            repository.bookMeeting(
                BookedMeeting(
                    exhibitorId = exhibitorId,
                    exhibitorName = exhibitorName,
                    meetingTime = selectedTime,
                    location = location,
                    purpose = purpose
                )
            )
        }
    }

    fun cancelMeeting(meetingId: Int) {
        viewModelScope.launch {
            repository.cancelMeeting(meetingId)
        }
    }

    // Contact scan / Lead retrieval
    fun scanContact(name: String, company: String, email: String, phone: String, initialRank: String = "Warm") {
        viewModelScope.launch {
            repository.addContact(
                ScannedContact(
                    id = email.ifEmpty { "contact_${System.currentTimeMillis()}" },
                    name = name,
                    company = company,
                    email = email,
                    phone = phone,
                    rank = initialRank,
                    notes = "Scanned at FFS 2026."
                )
            )
            // Add gamification points!
            awardPoints(40)
        }
    }

    fun updateContactDetails(id: String, notes: String, rank: String) {
        viewModelScope.launch {
            repository.updateContactNotes(id, notes, rank)
        }
    }

    fun deleteContact(id: String) {
        viewModelScope.launch {
            repository.removeContact(id)
        }
    }

    // Social Feed action
    fun postToSocialFeed(
        textContent: String,
        authorRole: String = "Attendee",
        customAuthor: String? = null,
        customCompany: String? = null
    ) {
         viewModelScope.launch {
             val currentProfile = repository.getProfile()
             val authorName = customAuthor ?: currentProfile?.name?.ifEmpty { "Anonymous Visitor" } ?: "Anonymous Visitor"
             val authorCompany = customCompany ?: currentProfile?.company?.ifEmpty { "FFS 2026" } ?: "FFS 2026"
             val roleToUse = if (customAuthor != null) authorRole else (currentProfile?.role ?: "Attendee")
             
             repository.addSocialPost(
                 SocialPost(
                     authorName = authorName,
                     authorCompany = authorCompany,
                     textContent = textContent,
                     likesCount = 0,
                     timestamp = System.currentTimeMillis(),
                     authorRole = roleToUse
                 )
             )
             awardPoints(30)
         }
    }

    fun toggleLikePost(post: SocialPost) {
        viewModelScope.launch {
            repository.toggleLikePost(post)
        }
    }

    fun deleteSocialPost(id: Int) {
        viewModelScope.launch {
            repository.deleteSocialPost(id)
        }
    }

    fun submitSpeakerProposal(speaker: SpeakerEntity) {
        viewModelScope.launch {
            repository.insertSpeaker(speaker)
        }
    }

    fun createAgendaSession(session: AgendaSession) {
        viewModelScope.launch {
            repository.insertAgendaSession(session)
        }
    }

    // Gamification state changes
    fun completeTrivia(questionText: String, selectedOption: String, isCorrect: Boolean) {
        viewModelScope.launch {
            val currentProfile = repository.getProfile()
            val userEmail = currentProfile?.email?.ifEmpty { "anonymous@example.com" } ?: "anonymous@example.com"
            val userName = currentProfile?.name?.ifEmpty { "Anonymous Visitor" } ?: "Anonymous Visitor"

            repository.saveTriviaAnswer(
                TriviaAnswerEntity(
                    userEmail = userEmail,
                    userName = userName,
                    questionText = questionText,
                    selectedOption = selectedOption,
                    isCorrect = isCorrect
                )
            )

            val current = gameState.value ?: GamificationState()
            val scoreBonus = if (isCorrect) 50 else 10
            val nextRaffles = if (isCorrect) current.raffleTickets + 2 else current.raffleTickets + 1
            repository.saveGameState(
                current.copy(
                    score = current.score + scoreBonus,
                    triviaPassed = true,
                    raffleTickets = nextRaffles
                )
            )
        }
    }

    fun submitPollVote(optionIndex: Int) {
        viewModelScope.launch {
            val current = gameState.value ?: GamificationState()
            if (!current.pollingCompleted) {
                repository.saveGameState(
                    current.copy(
                        score = current.score + 25,
                        pollingCompleted = true
                    )
                )
            }
        }
    }

    fun findScavengerHuntCode(exhibitorId: String) {
        viewModelScope.launch {
            val current = gameState.value ?: GamificationState()
            val list = current.scavengerExhibitorsFound.split(",").filter { it.isNotEmpty() }.toMutableList()
            if (!list.contains(exhibitorId)) {
                list.add(exhibitorId)
                val newListString = list.joinToString(",")
                repository.saveGameState(
                    current.copy(
                        score = current.score + 50,
                        scavengerExhibitorsFound = newListString,
                        raffleTickets = current.raffleTickets + 1
                    )
                )
            }
        }
    }

    fun saveOnboardedProfile(
        name: String,
        company: String,
        goal: String,
        track: String,
        tier: String,
        email: String,
        provider: String,
        role: String
    ) {
        viewModelScope.launch {
            val profile = ProfileEntity(
                id = 1,
                name = name,
                company = company,
                businessGoal = goal,
                preferredTrack = track,
                budgetTier = tier,
                email = email,
                provider = provider,
                isOtpVerified = true,
                role = role,
                isOnboarded = true
            )
            repository.saveProfile(profile)
            // Immediately run matchmaking search
            runMatchmaking(name, company, goal, track, tier)
        }
    }

    fun saveCsvOnboardedProfile(
        csv: CsvProfile,
        email: String,
        provider: String
    ) {
        viewModelScope.launch {
            // Determine best match parameters
            val track = when (csv.id) {
                "admin" -> "Tech & Innovation"
                "feb8a00c-839e-4412-80c8-2e76765a1014" -> "Tech & Innovation"
                "e1160788-009e-4db2-a61c-a134bfd84634" -> "Supply Chain & Automation"
                "f4f09cb2-c7c0-49c0-b776-225ee4155cdb" -> "Sustainability & Packaging"
                "9da28dbe-3b1e-4e60-834e-69e13f2ecf9c" -> "Tech & Innovation"
                else -> "Sustainability & Packaging"
            }
            val tier = when (csv.id) {
                "admin" -> "Enterprise"
                "feb8a00c-839e-4412-80c8-2e76765a1014" -> "Seed"
                "e1160788-009e-4db2-a61c-a134bfd84634" -> "Growth"
                "f4f09cb2-c7c0-49c0-b776-225ee4155cdb" -> "Enterprise"
                "9da28dbe-3b1e-4e60-834e-69e13f2ecf9c" -> "Growth"
                else -> "Growth"
            }
            val role = if (csv.id == "admin") "Admin" else "Exhibitor"

            val profile = ProfileEntity(
                id = 1,
                name = csv.displayName,
                company = csv.displayName,
                businessGoal = csv.companyDescription,
                preferredTrack = track,
                budgetTier = tier,
                email = email,
                provider = provider,
                isOtpVerified = true,
                role = role,
                isOnboarded = true,
                countryRegion = csv.countryRegion,
                websiteUrl = csv.websiteUrl,
                linkedinUrl = csv.linkedinUrl,
                instagramUrl = csv.instagramUrl,
                xUrl = csv.xUrl,
                annualRevenue = csv.annualRevenue,
                currentMarkets = csv.currentMarkets,
                targetMarkets = csv.targetMarkets,
                importExportStatus = csv.importExportStatus,
                brandsRepresented = csv.brandsRepresented,
                primarySectors = csv.primarySectors,
                targetBuyers = csv.targetBuyers,
                boothSizeConfirmed = csv.boothSizeConfirmed,
                electricalNeeds = csv.electricalNeeds,
                exhibitorLeadId = csv.exhibitorLeadId,
                uniqueId = csv.id
            )
            repository.saveProfile(profile)
            runMatchmaking(csv.displayName, csv.displayName, csv.companyDescription, track, tier)
        }
    }

    fun logOut() {
        viewModelScope.launch {
            val emptyProfile = ProfileEntity(
                id = 1,
                isOnboarded = false,
                isOtpVerified = false
            )
            repository.saveProfile(emptyProfile)
            resetMatchState()
        }
    }

    // Speakers Administration
    fun updateSpeaker(speaker: SpeakerEntity) {
        viewModelScope.launch {
            repository.addOrUpdateSpeaker(speaker)
        }
    }

    fun deleteSpeaker(id: String) {
        viewModelScope.launch {
            repository.deleteSpeaker(id)
        }
    }

    // Exhibitors Administration
    fun updateExhibitor(exhibitor: ExhibitorEntity) {
        viewModelScope.launch {
            repository.addOrUpdateExhibitor(exhibitor)
        }
    }

    fun deleteExhibitor(id: String) {
        viewModelScope.launch {
            repository.deleteExhibitor(id)
        }
    }

    // Attendees Administration
    fun updateAttendee(attendee: AttendeeEntity) {
        viewModelScope.launch {
            repository.addOrUpdateAttendee(attendee)
        }
    }

    fun deleteAttendee(id: String) {
        viewModelScope.launch {
            repository.deleteAttendee(id)
        }
    }

    fun createMatchmakerQuestion(questionText: String, track: String) {
        viewModelScope.launch {
            repository.insertMatchmakerQuestion(MatchmakerQuestion(questionText = questionText, track = track))
        }
    }

    fun deleteMatchmakerQuestion(id: Int) {
        viewModelScope.launch {
            repository.deleteMatchmakerQuestion(id)
        }
    }

    private suspend fun awardPoints(amount: Int) {
        val current = gameState.value ?: GamificationState()
        repository.saveGameState(current.copy(score = current.score + amount))
    }
}

sealed interface MatchUiState {
    object Idle : MatchUiState
    object Loading : MatchUiState
    data class Success(val result: B2BMatchResult) : MatchUiState
    data class Error(val message: String) : MatchUiState
}
